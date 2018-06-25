/**
 * Copyright 2018 人人开源 http://www.renren.io
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.lebaoxun.commons.utils;


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.lebaoxun.commons.exception.I18nMessageException;

/**
 * hibernate-validator校验工具类
 *
 * 参考文档：http://docs.jboss.org/hibernate/validator/5.4/reference/en-US/html_single/
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2017-03-15 10:50
 */
public class ValidatorUtils {
    private static Validator validator;

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * 校验对象
     * @param object        待校验对象
     * @param groups        待校验的组
     * @throws RRException  校验不通过，则报RRException异常
     */
    public static void validateEntity(Object object, Class<?>... groups)
            throws I18nMessageException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
        	ConstraintViolation<Object> constraint = (ConstraintViolation<Object>)constraintViolations.iterator().next();
            throw new I18nMessageException(constraint.getMessage());
        }
    }
    
    /**
	 * 验证只能输入字母或者数字
	 * @param email
	 * @return
	 */
	public static boolean checkNumberOrChar(String email) {
		boolean flag = false;
		try {
			String check = "[0-9A-Za-z]*";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 验证只能输入数字
	 * @param email
	 * @return
	 */
	public static boolean checkNumber(String email) {
		boolean flag = false;
		try {
			String check = "[0-9]*";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 校验微信号
	 * @param weChat
	 * @return
	 */
	public static boolean checkWechat(String weChat){
		
		boolean flag = false;
		try {
			String check = "[0-9a-zA-z-_]+$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(weChat);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	/**
	 * 校验手机号
	 * @param tel
	 * @return false true
	 */
	public static boolean checkTel(String tel){
		
		boolean flag = false;
		try {
			String check = "^((1[0-9]))\\d{9}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(tel);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	//校验金额
	public static boolean isBigDecimal(String str) {  
        java.util.regex.Matcher match =null;  
        if(checkNumber(str)==true){  
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[0-9]*");  
            match = pattern.matcher(str.trim());  
        }else{  
            if(str.trim().indexOf(".")==-1){  
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^[+-]?[0-9]*");  
                match = pattern.matcher(str.trim());  
            }else{  
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^[+-]?[0-9]+(\\.\\d{1,100}){1}");  
                match = pattern.matcher(str.trim());                  
            }  
        }  
        return match.matches();
	}
}
