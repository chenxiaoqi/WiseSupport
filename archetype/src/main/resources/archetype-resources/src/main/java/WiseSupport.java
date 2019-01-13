#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Author chenxiaoqi on 2018/12/22.
 */

@SpringBootApplication(scanBasePackages = "${package}")
@ImportResource("/spring/applicationContext.xml")
public class WiseSupport {

    public static void main(String[] args) {
        SpringApplication.run(WiseSupport.class, args);
    }
}
