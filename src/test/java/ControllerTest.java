import controllers.*;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.BDDMockito.given;

public class ControllerTest {

    @Test (expected = Exception.class)
    public void cpuController_FailedToGetSessions() {
        final cpuController controller = Mockito.spy(new cpuController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test
    public void cpuController_SucceededToGetSessions() {
        final cpuController controller = Mockito.spy(new cpuController());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void cpuController_FailedToGetColumns() {
        final cpuController controller = Mockito.spy(new cpuController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void memoryController_FailedToGetSessions() {
        final memoryController controller = Mockito.spy(new memoryController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void memoryController_FailedToGetColumns() {
        final memoryController controller = Mockito.spy(new memoryController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void networkController_FailedToGetSessions() {
        final networkController controller = Mockito.spy(new networkController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void networkController_FailedToGetColumns() {
        final networkController controller = Mockito.spy(new networkController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void powerController_FailedToGetSessions() {
        final powerController controller = Mockito.spy(new powerController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void processController_FailedToGetSessions() {
        final processController controller = Mockito.spy(new processController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void processController_FailedToGetColumns() {
        final processController controller = Mockito.spy(new processController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void processInfoController_FailedToGetNames() {
        final processinfoController controller = Mockito.spy(new processinfoController());
        given(controller.getProcessesNames()).willThrow(new Exception());
        controller.getProcessesNames();
    }

    @Test (expected = Exception.class)
    public void processInfoController_FailedToGetSessions() {
        final processinfoController controller = Mockito.spy(new processinfoController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void processInfoController_FailedToGetColumns() {
        final processinfoController controller = Mockito.spy(new processinfoController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }

    @Test (expected = Exception.class)
    public void sensorsController_FailedToGetSessions() {
        final sensorsController controller = Mockito.spy(new sensorsController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void sensorsController_FailedToGetColumns() {
        final sensorsController controller = Mockito.spy(new sensorsController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }
}