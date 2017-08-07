/**
 * Project Name:design
 * File Name:TestFactory.java
 * Package Name:com.lyy.Factory
 * Date:2017年5月27日下午1:01:07
 * Copyright (c) 2017, fengyun.lyy@foxmail.com All Rights Reserved.
 *
*/

package com.lyy.Factory;
/**
 * ClassName:TestFactory <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:   TODO ADD REASON. <br/>
 * Date:     2017年5月27日 下午1:01:07 <br/>
 * @author   liyayong
 * @version
 * @since    JDK 1.7
 * @see
 */
public class TestFactory {
    public static void main(String[] args) {
        Shape shape = ShapeFactory.getShape(Square.class);
        shape.draw();
        shape = ShapeFactory.getShape(Rectangle.class);
        shape.draw();
    }
}
