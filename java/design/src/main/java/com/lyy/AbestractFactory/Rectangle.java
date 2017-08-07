/**
 * Project Name:design
 * File Name:Rectangle.java
 * Package Name:com.lyy.Factory
 * Date:2017年5月27日下午12:54:32
 * Copyright (c) 2017, fengyun.lyy@foxmail.com All Rights Reserved.
 *
*/

package com.lyy.AbestractFactory;
/**
 * ClassName:Rectangle <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:   TODO ADD REASON. <br/>
 * Date:     2017年5月27日 下午12:54:32 <br/>
 * @author   liyayong
 * @version
 * @since    JDK 1.7
 * @see
 */
public class Rectangle implements Shape {

    @Override
    public void draw() {
        System.out.println("Rectangle draw => "+ Rectangle.class.getName());
    }

}
