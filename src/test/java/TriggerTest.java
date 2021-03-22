import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TriggerTest {
    @Test
    public void whenReturnOne() {
        assertEquals(1, new Trigger().logic());
    }

}