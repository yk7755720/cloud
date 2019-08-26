/**
 * 
 */
package com.common.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Administrator
 *
 */
public class YKSBeanUtils {
	public static String[] getNotNullPropertyNames(Object target) {
		final BeanWrapper src = new BeanWrapperImpl(target);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> notEmptyNames = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (!ObjectUtils.isEmpty(srcValue)) {
				if (srcValue instanceof String && StringUtils.isEmpty(srcValue)) {
					continue;
				}
				notEmptyNames.add(pd.getName());
			}
		}
		String[] result = new String[notEmptyNames.size()];
		return notEmptyNames.toArray(result);
	}

	public static void copyProperties(Object src, Object target) {
		BeanUtils.copyProperties(src, target, getNotNullPropertyNames(target));
	}
}
