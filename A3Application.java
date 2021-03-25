package ro.tuc.ds2020;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import ro.tuc.ds2020.services.rpc.PillDispenser;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ro.tuc.ds2020.services.rpc.PillDispenserService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Configuration
@SpringBootApplication
@ComponentScan("ro.tuc.ds2020")
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class A3Application {

	@Bean
	public HttpInvokerProxyFactoryBean invoker() {
		HttpInvokerProxyFactoryBean invoker = new HttpInvokerProxyFactoryBean();
		invoker.setServiceUrl("https://ds2020-alina-assign1-backend.herokuapp.com/pillDispenser");
		invoker.setServiceInterface(PillDispenser.class);
		return invoker;
	}

	public static void main(String[] args) {
		PillDispenser pillDispenser = SpringApplication.run(A3Application.class, args).getBean(PillDispenser.class);
		new PillDispenserService(pillDispenser);
		Application.launch(JavaFxApplication.class, args);
	}

}
