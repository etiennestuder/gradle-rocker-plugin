import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.runtime.StringBuilderOutput;

public class Main {

    public static void main(String[] args) {
        RockerModel model = Hello.template("Switzerland");
        StringBuilderOutput output = model.render(StringBuilderOutput.FACTORY);
        System.out.println(output.getBuffer().toString());
    }

}
