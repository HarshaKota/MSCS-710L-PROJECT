import controllers.*;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.BDDMockito.given;

public class ControllerTest {

    @Test (expected = Exception.class)
    public void cpuControllerFailedToGetSessions() {
        final cpuController controller = Mockito.spy(new cpuController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test
    public void cpuControllerSucceededToGetSessions() {
        final cpuController controller = Mockito.spy(new cpuController());
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
    public void networkMemoryControllerFailedToGetColumns() {
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
    public void processInfoControllerFailedToGetNames() {
        final processinfoController controller = Mockito.spy(new processinfoController());
        given(controller.getProcessesNames()).willThrow(new Exception());
        controller.getProcessesNames();
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

    @Test (expected = Exception.class)
    public void sensorsControllerFailedToGetSessions() {
        final sensorsController controller = Mockito.spy(new sensorsController());
        given(controller.getSessions()).willThrow(new Exception());
        controller.getSessions();
    }

    @Test (expected = Exception.class)
    public void sensorsControllerFailedToGetColumns() {
        final sensorsController controller = Mockito.spy(new sensorsController());
        given(controller.getColumns()).willThrow(new Exception());
        controller.getColumns();
    }
}