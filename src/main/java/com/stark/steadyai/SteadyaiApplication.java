package com.stark.steadyai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SteadyaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SteadyaiApplication.class, args);
	}

//    @org.springframework.context.annotation.Bean(initMethod = "start", destroyMethod = "stop")
//    public org.h2.tools.Server h2WebConsoleServer() throws java.sql.SQLException {
//        return org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
//    }

}
