package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories("application/repositories")
@EntityScan( basePackages = {"application/entities"} )
@ComponentScan( basePackages = {"application"} )
public class Server {

    public static void main(String[] args){
        SpringApplication.run(Server.class, args);
    }
}
