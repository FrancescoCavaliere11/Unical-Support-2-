package unical_support.unicalsupport2.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.EnableCommand;
import unical_support.unicalsupport2.commands.CategoryCommand;

@Configuration
@EnableCommand(CategoryCommand.class)
public class SpringShellConfig {
}
