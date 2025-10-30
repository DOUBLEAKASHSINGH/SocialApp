/*
* Class name
*	PageResponseDto
*
* Version info
*	JavaSE-11
*
* Copyright notice
* 
* Author info
*	Name: Sharad Jain
*	Email-ID: sharad.jain@nagarro.com
*
* Creation date
* 	11-06-2023
*
* Last updated By
* 	Sharad Jain
*
* Last updated Date
* 	11-06-2023
*
* Description
* 	This class is for response data transfer.
*/

package ac.in.iiitd.fcs29.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * This class is for response data transfer.
 * 
 * @author sharadjain
 *
 */
@Setter
@Getter
public class PageResponseDto<T> implements Serializable {
	@Serial
	private static final long serialVersionUID = -6150216357957138321L;

    private List<T> items;
    private int pageSize;
    private long totalSize;

}
