package com.energia.backend.repository.aneel;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.Subestacao;

public interface SubestacaoRepository extends JpaRepository<Subestacao, Long> {
}