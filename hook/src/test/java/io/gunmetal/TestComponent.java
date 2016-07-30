package io.gunmetal;

/**
* @author rees.byars
*/
public interface TestComponent extends SubComponent {

    default void grr() {
        System.out.println("grr!");
    }

    default Object object(String whoa) {
        System.out.println("object!" + whoa);
        return o(whoa);
    }

    default Object oooo(String whoa) {
        System.out.println("oooo!" + whoa);
        return whoa;
    }

    default Object yoiks(String whoa) {
        System.out.println("wha?!" + whoa);
        return new Object();
    }

}
