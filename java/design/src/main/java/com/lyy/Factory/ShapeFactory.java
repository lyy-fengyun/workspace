/**
 * Project Name:design
 * File Name:ShapFactory.java
 * Package Name:com.lyy.Factory
 * Date:2017年5月27日下午12:56:31
 * Copyright (c) 2017, fengyun.lyy@foxmail.com All Rights Reserved.
 *
*/

package com.lyy.Factory;
/**
 * ClassName:ShapFactory <br/>
 * Function: 静态工厂类，通过给定的类对象生成类实例 <br/>
 * Reason:   TODO ADD REASON. <br/>
 * Date:     2017年5月27日 下午12:56:31 <br/>
 * @author   liyayong
 * @version
 * @since    JDK 1.7
 * @see
 */
public class ShapeFactory {


    public static Shape getShape(Class<? extends Shape> clazz){
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

}
