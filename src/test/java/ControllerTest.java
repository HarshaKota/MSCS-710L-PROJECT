import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.BDDMockito.given;

public class ControllerTest {

    @BeforeClass
    public static void setup() {
    }

    @Test (expected = Exception.class)
    public void cpuControllerFailedToGetSessions() {
        final cpuController controller = Mockito.spy(new cpuController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

}