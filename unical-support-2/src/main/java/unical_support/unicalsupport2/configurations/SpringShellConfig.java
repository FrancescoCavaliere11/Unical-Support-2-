package unical_support.unicalsupport2.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.EnableCommand;
import unical_support.unicalsupport2.commands.CategoryCommand;
import unical_support.unicalsupport2.commands.EmailCommand;

@Configuration
@EnableCommand({CategoryCommand.class, EmailCommand.class})
public class SpringShellConfig {
}
