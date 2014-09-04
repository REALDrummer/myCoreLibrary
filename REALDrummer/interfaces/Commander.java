package REALDrummer.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Commander {
    public String usage();

    public boolean op_command() default true;

    public boolean user_command() default true;

    public boolean console_command() default true;

    public boolean open_command() default false;
}
