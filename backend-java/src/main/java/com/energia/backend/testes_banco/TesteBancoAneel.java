package com.energia.backend.testes_banco;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.energia.backend.model.aneel.*;

import com.energia.backend.repository.aneel.*;

import jakarta.transaction.Transactional;


@Configuration
public class TesteBancoAneel {

    @Bean
    @Transactional
    CommandLineRunner test(
            DistribuidoraRepository distribuidoraRepo,
            ConjuntoRepository conjuntoRepo,
            LimitesRepository limitesRepo,
            SigIndicadorRepository sigIndicadorRepo,
            MetricasRepository metricasRepo,
            PerdasRepository perdasRepo,
            SubestacaoRepository subestacaoRepo
    ) {
        return args -> {

            System.out.println("🔥 ENTROU NO RUNNER");

            if (distribuidoraRepo.existsByCodigoIdDist(1000L)) {
                System.out.println("Seed ANEEL ja existente, pulando insercao inicial.");
                return;
            }

            System.out.println("Qtd distribuidoras antes: " + distribuidoraRepo.count());
            // =========================
            // 1. Distribuidora
            // =========================
            Distribuidora dist = new Distribuidora();
            dist.setCodigoIdDist(1000L);
            dist.setSigAgente("TESTE_DIST");
            dist.setNumCnpj("12345678901234");
            dist.setRegiao(Regiao.SUDESTE);
            dist.setUf("SP");
            dist.setContractType(ContractType.CONCESSIONARIA);

            dist = distribuidoraRepo.save(dist);

            System.out.println("Distribuidora salva com ID: " + dist.getId());
            System.out.println("Qtd distribuidoras depois: " + distribuidoraRepo.count());

            // =========================
            // 2. Conjunto
            // =========================
            Conjunto conjunto = new Conjunto();
            conjunto.setIdeConjUndConsumidoras(999L);
            conjunto.setDscConjUndConsumidoras("Conjunto Teste");
            conjunto.setDistribuidora(dist);

            conjunto = conjuntoRepo.save(conjunto);

            // =========================
            // 3. Limites
            // =========================
            Limites limites = new Limites();
            limites.setConjunto(conjunto);
            limites.setAno(2024L);
            limites.setDecLim(10.5);
            limites.setFecLim(5.2);

            limitesRepo.save(limites);

            // =========================
            // 4. SigIndicador
            // =========================
            SigIndicador sig = new SigIndicador();
            sig.setIndicadorType(IndicadorType.DEC);

            sig = sigIndicadorRepo.save(sig);

            // =========================
            // 5. Metricas
            // =========================
            Metricas metrica = new Metricas();
            metrica.setConjunto(conjunto);
            metrica.setSigIndicador(sig);
            metrica.setNumPeriodoIndice(1L);
            metrica.setAnoIndice(2024L);
            metrica.setDataGeracaoConjDados(LocalDate.now());
            metrica.setVlrIndiceEnviado(12.34);

            metricasRepo.save(metrica);

            // =========================
            // 6. Perdas
            // =========================
            Perdas perdas = new Perdas();
            perdas.setDistribuidora(dist);
                    
            LocalDate data = LocalDate.now();
            perdas.setDataProcesso(data);
                    
            // importante agora por causa da migration
            perdas.setAno((long) data.getYear());
                    
            perdas.setTme(1.2);
            perdas.setPerdasRedeBasica(2.3);
            perdas.setCustoPerdasRedeBasica(3.4);
                    
            perdasRepo.save(perdas);

            // =========================
            // 7. Subestacao
            // =========================
            Subestacao sub = new Subestacao();
            sub.setCodIdSub("SUB_TESTE");
            sub.setNameSub("Subestacao Teste");
            sub.setDistribuidora(dist);

            subestacaoRepo.save(sub);

            System.out.println("TESTE FINALIZADO COM SUCESSO!");
        };
    }
}