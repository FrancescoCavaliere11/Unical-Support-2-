    package unical_support.unicalsupport2.configurations;

    import org.springframework.context.annotation.Configuration;
    import org.springframework.shell.command.annotation.EnableCommand;
    import unical_support.unicalsupport2.commands.*;

    @Configuration
    @EnableCommand({
            CategoryCommand.class,
            EmailCommand.class,
            LlmCommand.class,
            TemplateCommand.class,
            PromptCommand.class,
            DocumentCommand.class,
    })
    public class SpringShellConfig {
    }
