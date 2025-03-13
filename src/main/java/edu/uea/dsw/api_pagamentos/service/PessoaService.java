package edu.uea.dsw.api_pagamentos.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uea.dsw.api_pagamentos.dto.EnderecoDTO;
import edu.uea.dsw.api_pagamentos.dto.PessoaDTO;
import edu.uea.dsw.api_pagamentos.model.Endereco;
import edu.uea.dsw.api_pagamentos.model.Pessoa;
import edu.uea.dsw.api_pagamentos.repository.PessoaRepository;
import edu.uea.dsw.api_pagamentos.service.exception.RecursoEmUsoException;
import edu.uea.dsw.api_pagamentos.service.exception.RecursoNaoEncontradoException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    // Método para converter Pessoa em PessoaDTO
    private PessoaDTO toDTO(Pessoa pessoa) {
        PessoaDTO dto = new PessoaDTO();
        dto.setCodigo(pessoa.getCodigo());
        dto.setNome(pessoa.getNome());
        dto.setAtivo(pessoa.getAtivo());
        if (pessoa.getEndereco() != null) {
            EnderecoDTO enderecoDTO = new EnderecoDTO();
            enderecoDTO.setLogradouro(pessoa.getEndereco().getLogradouro());
            enderecoDTO.setCidade(pessoa.getEndereco().getCidade());
            enderecoDTO.setEstado(pessoa.getEndereco().getEstado());
            enderecoDTO.setCep(pessoa.getEndereco().getCep());
            dto.setEndereco(enderecoDTO);
        }
        return dto;
    }

    // Método para converter PessoaDTO em Pessoa
    private Pessoa toEntity(PessoaDTO dto) {
        Pessoa pessoa = new Pessoa();
        pessoa.setCodigo(dto.getCodigo());
        pessoa.setNome(dto.getNome());
        pessoa.setAtivo(dto.getAtivo());
        if (dto.getEndereco() != null) {
            Endereco endereco = new Endereco();
            endereco.setLogradouro(dto.getEndereco().getLogradouro());
            endereco.setCidade(dto.getEndereco().getCidade());
            endereco.setEstado(dto.getEndereco().getEstado());
            endereco.setCep(dto.getEndereco().getCep());
            pessoa.setEndereco(endereco);
        }
        return pessoa;
    }

    @Transactional
    public PessoaDTO criarPessoa(PessoaDTO pessoaDTO) {
        Pessoa pessoa = toEntity(pessoaDTO);
        Pessoa savedPessoa = pessoaRepository.save(pessoa);
        return toDTO(savedPessoa);
    }

    public PessoaDTO buscarPessoaPorCodigo(Long codigo) {
        Pessoa pessoa = pessoaRepository.findById(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
        return toDTO(pessoa);
    }

    public List<PessoaDTO> listarPessoas() {
        return pessoaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PessoaDTO atualizarPessoa(Long codigo, PessoaDTO pessoaDTO) {
        Pessoa pessoaExistente = pessoaRepository.findById(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));

        pessoaExistente.setNome(pessoaDTO.getNome());
        pessoaExistente.setAtivo(pessoaDTO.getAtivo());
        if (pessoaDTO.getEndereco() != null) {
            Endereco endereco = new Endereco();
            endereco.setLogradouro(pessoaDTO.getEndereco().getLogradouro());
            endereco.setCidade(pessoaDTO.getEndereco().getCidade());
            endereco.setEstado(pessoaDTO.getEndereco().getEstado());
            endereco.setCep(pessoaDTO.getEndereco().getCep());
            pessoaExistente.setEndereco(endereco);
        }

        Pessoa updatedPessoa = pessoaRepository.save(pessoaExistente);
        return toDTO(updatedPessoa);
    }

    @Transactional
    public void deletarPessoa(Long codigo) {
        if (!pessoaRepository.existsById(codigo)) {
            throw new RecursoNaoEncontradoException("Pessoa não encontrada");
        }
        try {
            pessoaRepository.deleteById(codigo);
        } catch (DataIntegrityViolationException e) {
            throw new RecursoEmUsoException("Pessoa em uso e não pode ser removida");
        }

    }
}