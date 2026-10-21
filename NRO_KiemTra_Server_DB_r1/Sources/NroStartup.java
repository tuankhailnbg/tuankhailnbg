import java.lang.reflect.*;
import java.nio.file.*;
import java.util.jar.*;

public class NroStartup {
    public static void main(String[] args) throws Exception {
        if(args.length!=4) { System.err.println("Usage: NroStartup JAR PROJECT_ROOT MODULE LOG_PATH"); System.exit(2); }
        Path jarPath=Path.of(args[0]);
        NroSupport.Settings settings=new NroSupport.Settings(Path.of(args[1]));
        NroSupport.capture(Path.of(args[3]),settings.secrets);
        String expected="login".equals(args[2]) ? "server.Server" : "nro.server.ServerManager";
        try(JarFile jar=new JarFile(jarPath.toFile())) {
            String actual=jar.getManifest().getMainAttributes().getValue("Main-Class");
            if(!expected.equals(actual)) throw new IllegalArgumentException("Unexpected Main-Class="+actual+", expected="+expected);
        }
        System.out.println("Module: "+args[2]+", JAR: "+jarPath);
        System.out.println("Working directory: "+Path.of("").toAbsolutePath());
        System.out.println("Startup output is recorded with configured secrets hidden.");
        System.out.println("Normal console input is preserved. Type baotri for normal shutdown.");
        try { Class.forName(expected).getMethod("main",String[].class).invoke(null,(Object)new String[0]); }
        catch(InvocationTargetException e) { e.getCause().printStackTrace(); System.exit(1); }
    }
}
