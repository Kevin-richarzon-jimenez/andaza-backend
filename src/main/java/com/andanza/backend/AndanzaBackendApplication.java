package com.andanza.backend;

import com.andanza.backend.entity.ConnectionCheck;
import com.andanza.backend.repository.ConnectionCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AndanzaBackendApplication {

	private static final Logger log = LoggerFactory.getLogger(AndanzaBackendApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AndanzaBackendApplication.class, args);
	}

	// Prueba temporal de la conexion a la base: inserta un registro si la tabla
	// esta vacia y muestra todo lo que hay. Se saca cuando exista la primera
	// entidad real del negocio.
	@Bean
	CommandLineRunner verifyDbConnection(ConnectionCheckRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.save(new ConnectionCheck(null, "conexion ok"));
			}
			log.info("Registros en connection_check: {}", repository.findAll());
		};
	}

}
