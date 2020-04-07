package com.practice.algorithm.leetcode_passing;

/**
 * @author Higmin
 * @date 2020/4/7 9:47
 *
 * 给定一个二叉树，找出其最大深度。
 *
 * 二叉树的深度为根节点到最远叶子节点的最长路径上的节点数。
 *
 * 说明: 叶子节点是指没有子节点的节点。
 *
 * 示例：
 * 给定二叉树 [3,9,20,null,null,15,7]，
 *
 *     3
 *    / \
 *   9  20
 *     /  \
 *    15   7
 * 返回它的最大深度 3 。
 *
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/maximum-depth-of-binary-tree
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 **/

import io.swagger.models.auth.In;
import javafx.util.Pair;

import java.util.Stack;

/** Definition for a binary tree node. */
class TreeNode {
	int val;
	TreeNode left;
	TreeNode right;
	TreeNode(int x) { val = x; }
}

public class N_104_MaximumDepthOfBinaryTree {
	/**
	 * 实现方法一：递归
	 * @param root
	 * @return
	 */
	public int maxDepth_1(TreeNode root){
		if (root == null) return 0;
		int left = maxDepth_1(root.left) + 1;
		int right = maxDepth_1(root.right) + 1;
		return Math.max(left, right);
	}

	/**
	 * 实现方法二：深度优先遍历: 采用先序遍历 --> 根 左 右 压入栈中
	 * @param root
	 * @return
	 */
	public int maxDepth_2(TreeNode root){
		if (root == null) return 0;
		Stack<Pair<TreeNode, Integer>> stack = new Stack<>();
		Pair<TreeNode, Integer> pair = new Pair<TreeNode, Integer>(root, 1); // [Pair 是一个简单键值对，类似于map,但只存放一组键值对，可以实现getKey()，getValue()]
		stack.push(pair);
		int res = 0;
		while(!stack.isEmpty()){
			Pair<TreeNode, Integer> cur = stack.pop();
			TreeNode curNode = cur.getKey();
			Integer curValue = cur.getValue();
			res = Math.max(res, curValue);
			if (curNode.left != null){
				Pair<TreeNode, Integer> leftNode = new Pair<>(curNode.left, curValue + 1);
				stack.push(leftNode);
			}
			if (curNode.right != null){
				Pair<TreeNode, Integer> rightNode = new Pair<>(curNode.right, curValue + 1);
				stack.push(rightNode);
			}
		}
		return res;
	}
}
