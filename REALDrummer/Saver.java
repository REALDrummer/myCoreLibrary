package REALDrummer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Saver {
    public String data_type() default "";

    public boolean settings() default false;
}
