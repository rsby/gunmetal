package io.gunmetal;

/**
 * @author rees.byars
 */
interface SubComponent {

    default Object o(String whoa) {
        System.out.println("o!" + whoa);
        return yoiks(whoa);
    }

    Object yoiks(String whoa);

}
