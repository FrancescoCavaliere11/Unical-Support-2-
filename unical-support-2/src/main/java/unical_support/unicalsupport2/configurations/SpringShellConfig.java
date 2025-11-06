package unical_support.unicalsupport2.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.EnableCommand;
import unical_support.unicalsupport2.commands.CategoryCommand;
import unical_support.unicalsupport2.commands.EmailCommand;
import unical_support.unicalsupport2.commands.ModelCommand;

@Configuration
@EnableCommand({CategoryCommand.class, EmailCommand.class, ModelCommand.class})
public class SpringShellConfig {
}
