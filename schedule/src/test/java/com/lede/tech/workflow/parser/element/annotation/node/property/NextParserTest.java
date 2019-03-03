package com.lede.tech.workflow.parser.element.annotation.node.property;

import com.lede.tech.workflow.parser.element.annotation.node.property.NextParser;
import com.lede.tech.workflow.parser.element.bean.Branch;

public class NextParserTest
{
	public static void main(String[] args)
	{
		NextParser np = new NextParser("(c1:n1,c2:(c4:[n5,[n8,n9]],c6:n6),c3:n3)");
		Branch b = (Branch) np.parse();
		System.out.println(b.getSubBranchSize());

		System.out.println();

		System.out.print(Branch.print(b));
	}
}
