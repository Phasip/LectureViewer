package com.phasip.lectureview;

import java.io.Serializable;


public class Pair<L extends Serializable,Ri extends Serializable> implements Serializable {

	private static final long serialVersionUID = 4773405542094760147L;
	private final L left;
	  private final Ri right;

	  public Pair(L left, Ri right) {
	    this.left = left;
	    this.right = right;
	  }

	  public L getLeft() { return left; }
	  public Ri getRight() { return right; }

	  @Override
	  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

	  @Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof Pair)) return false;
	    @SuppressWarnings("rawtypes")
		Pair pairo = (Pair) o;
	    return this.left.equals(pairo.getLeft()) &&
	           this.right.equals(pairo.getRight());
	  }

	}
