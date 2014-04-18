package REALDrummer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Commander {
    public boolean user_command() default true;

    public boolean console_command() default true;
}
