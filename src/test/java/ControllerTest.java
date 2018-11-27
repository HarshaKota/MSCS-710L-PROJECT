import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @Test (expected = Exception.class)
    public void cpuControllerFailedToGetColumns() {
        final cpuController controller = Mockito.spy(new cpuController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void memoryControllerFailedToGetSessions() {
        final memoryController controller = Mockito.spy(new memoryController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void memoryControllerFailedToGetColumns() {
        final memoryController controller = Mockito.spy(new memoryController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void networkControllerFailedToGetSessions() {
        final networkController controller = Mockito.spy(new networkController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void networkmemoryControllerFailedToGetColumns() {
        final networkController controller = Mockito.spy(new networkController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void powerControllerFailedToGetSessions() {
        final powerController controller = Mockito.spy(new powerController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void processControllerFailedToGetSessions() {
        final processController controller = Mockito.spy(new processController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void processControllerFailedToGetColumns() {
        final processController controller = Mockito.spy(new processController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void processInfoControllerFailedToGetSessions() {
        final processinfoController controller = Mockito.spy(new processinfoController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void processInfoControllerFailedToGetColumns() {
        final processinfoController controller = Mockito.spy(new processinfoController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }
}