package io.gunmetal.sandbox.testmocks.dongle.dal;

import io.gunmetal.Inject;
import io.gunmetal.Module;
import io.gunmetal.Overrides;
import io.gunmetal.Supplies;
import io.gunmetal.sandbox.testmocks.dongle.layers.Bl;
import io.gunmetal.sandbox.testmocks.dongle.layers.Dal;
import io.gunmetal.sandbox.testmocks.dongle.layers.Ui;

/**
 * @author rees.byars
 */
@Overrides(allowPluralQualifier = true, allowCycle = true)
@Dal
@Module(notAccessibleFrom = DalModule.BlackList.class)
public class DalModule {

    @Ui
    class BlackList {
    }

    @Inject @Overrides(allowFieldInjection = true, allowCycle = true, allowPluralQualifier = true)
    @Dal @Bl DalModule dalModule;

    @Supplies public DongleDao dongleDao() {
        return new DongleDao() {
        };
    }

    @Supplies @Overrides(allowPluralQualifier = true, allowCycle = true) @Bl public DalModule dalModule() {
        return this;
    }

}
