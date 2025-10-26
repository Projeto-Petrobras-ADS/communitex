package br.senai.sc.communitex.service;


import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.dto.AdocaoStatusResponseDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.InvalidAdocaoException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdocaoService {

    private final AdocaoRepository adocaoRepository;
    private final EmpresaRepository empresaRepository;
    private final PracaRepository pracaRepository;

    public AdocaoService(AdocaoRepository adocaoRepository,
                         EmpresaRepository empresaRepository,
                         PracaRepository pracaRepository){
        this.adocaoRepository = adocaoRepository;
        this.empresaRepository = empresaRepository;
        this.pracaRepository = pracaRepository;
    }


    public List<AdocaoResponseDTO> findAll(){
        return adocaoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AdocaoResponseDTO create(AdocaoRequestDTO dto){
        Empresa empresa = empresaRepository.findById(dto.empresa().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: "+ dto.empresa().getId()));
        Praca praca = pracaRepository.findById(dto.praca().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + dto.praca().getId()));


        if (praca.getStatus() == StatusPraca.ADOTADA) {
            throw new InvalidAdocaoException("Esta praça já está adotada por outra empresa!");
        }

        Adocao adocao = new Adocao();
        adocao.setDataInicio(dto.dataInicio());
        adocao.setDataFim(dto.dataFim());
        adocao.setDescricaoProjeto(dto.descricaoProjeto());
        adocao.setStatus(dto.status());
        adocao.setEmpresa(empresa);
        adocao.setPraca(praca);
        praca.setStatus(StatusPraca.ADOTADA);
        pracaRepository.save(praca);


        adocaoRepository.save(adocao);

        return  toResponseDTO(adocao);
    }

    public AdocaoResponseDTO findById(Long id){
        Adocao adocao = adocaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adoção não encontrada com ID: " + id));

        return toResponseDTO(adocao);

    }

    public List<AdocaoStatusResponseDTO> findByStatus(StatusAdocao status){
        List<Adocao> adocaos = adocaoRepository.findByStatus(status);

        return adocaos.stream()
                .map(adocao -> new AdocaoStatusResponseDTO(
                        adocao.getId(),
                        adocao.getDescricaoProjeto(),
                        adocao.getDataInicio(),
                        adocao.getDataFim(),
                        adocao.getStatus(),
                        adocao.getEmpresa() != null ? adocao.getEmpresa().getNomeFantasia() : null,
                        adocao.getPraca() != null ? adocao.getPraca().getNome() : null
                )).collect(Collectors.toList());

    }

    public List<AdocaoResponseDTO> findByPeriodo(LocalDate inicio, LocalDate fim) {
        if(inicio == null || fim == null) {
            throw new IllegalStateException("As datas de inicio e fim são obrigatorios");
        }

        if(fim.isBefore(inicio)){
            throw new IllegalStateException("A data final não pode ser anterior à data inicial");
        }

        List<Adocao> adocaos = adocaoRepository.findByPeriodo(inicio, fim);

        return  adocaos.stream().map(this::toResponseDTO).toList();
    }

    public List<AdocaoResponseDTO> findByEmpresa(Long empresaId) {
        List<Adocao> adocaos = adocaoRepository.findByEmpresaId(empresaId);
        return adocaos.stream().map(this::toResponseDTO).toList();
    }

    public List<AdocaoResponseDTO> findAdocoesByPrazoEStatus(Integer dias, StatusAdocao status){
        if(dias == null || dias <= 0){
            dias = 7;
        }
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);

        List<Adocao> adocaos = adocaoRepository.findAdocoesByPrazoEStatus(hoje, limite, status);
        return adocaos.stream().map(this::toResponseDTO).toList();

    }

    public List<AdocaoResponseDTO> findByPraca(Long pracaId) {
        List<Adocao> adocaos = adocaoRepository.findByPraca(pracaId);
        return adocaos.stream().map(this::toResponseDTO).toList();
    }

    public AdocaoResponseDTO update(Long id, AdocaoRequestDTO dto){
        Adocao adocao = adocaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adoção não encontrada com ID: " + id));

        Empresa empresa = empresaRepository.findById(dto.empresa().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + dto.empresa().getId()));

        Praca praca = pracaRepository.findById(dto.empresa().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + dto.praca().getId()));

        adocao.setDataInicio(dto.dataInicio());
        adocao.setDataFim(dto.dataFim());
        adocao.setDescricaoProjeto(dto.descricaoProjeto());
        adocao.setPraca(praca);
        adocao.setEmpresa(empresa);

        adocaoRepository.save(adocao);

        return toResponseDTO(adocao);
    }

    public void delete(Long id){
        if(!adocaoRepository.existsById(id)){
            throw new ResourceNotFoundException("Praça não encontrado com ID: " + id);
        }
        adocaoRepository.deleteById(id);
    }

    public AdocaoResponseDTO finalizeAdoption(Long id){
        Adocao adocao = adocaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Adoção não encontrada com ID: " + id));
        adocao.setStatus(StatusAdocao.FINALIZADA);
        adocao.setDataFim(LocalDate.now());

        Praca praca = adocao.getPraca();
        praca.setStatus(StatusPraca.DISPONIVEL);

        adocaoRepository.save(adocao);

        return toResponseDTO(adocao);

    }


    private AdocaoResponseDTO toResponseDTO(Adocao adocao){
        return new AdocaoResponseDTO(
                adocao.getId(),
                adocao.getDataInicio(),
                adocao.getDataFim(),
                adocao.getDescricaoProjeto(),
                adocao.getStatus(),
                adocao.getEmpresa(),
                adocao.getPraca()
        );
    }



}
