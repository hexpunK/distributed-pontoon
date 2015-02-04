package distributedpontoon;

import distributedpontoon.client.GameTest;
import distributedpontoon.shared.CardTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Jordan
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CardTest.class,
    GameTest.class
})
public class AllTests { }
