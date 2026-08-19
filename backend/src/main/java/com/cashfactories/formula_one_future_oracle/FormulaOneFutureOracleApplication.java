package com.cashfactories.formula_one_future_oracle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FormulaOneFutureOracleApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormulaOneFutureOracleApplication.class, args);
	}

}
