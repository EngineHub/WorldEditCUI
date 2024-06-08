/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a 3d vector.
 */
public class Vector3 implements Comparable<Vector3>
{
	
	/**
	 * Vector with all elements set to 0. (0, 0, 0)
	 */
	public final static Vector3 ZERO = new Vector3(0, 0, 0);
	/**
	 * Unit Vector in the X direction. (1, 0, 0)
	 */
	public final static Vector3 UNIT_X = new Vector3(1, 0, 0);
	/**
	 * Unit Vector facing Forward. (1, 0, 0)
	 */
	public final static Vector3 Forward = UNIT_X;
	/**
	 * Unit Vector in the Y direction. (0, 1, 0)
	 */
	public final static Vector3 UNIT_Y = new Vector3(0, 1, 0);
	/**
	 * Unit Vector pointing Up. (0, 1, 0)
	 */
	public final static Vector3 Up = UNIT_Y;
	/**
	 * Unit Vector in the Z direction. (0, 0, 1)
	 */
	public final static Vector3 UNIT_Z = new Vector3(0, 0, 1);
	/**
	 * Unit Vector pointing Right. (0, 0, 1)
	 */
	public final static Vector3 Right = UNIT_Z;
	/**
	 * Unit Vector with all elements set to 1. (1, 1, 1)
	 */
	public final static Vector3 ONE = new Vector3(1, 1, 1);
	protected double x, y, z;
	
	/**
	 * Constructs a new Vector3 for the specified entity's position
	 * 
	 * @param entity
	 * @param partialTicks
	 */
	public Vector3(Entity entity, double partialTicks)
	{
		this(entity.getEyePosition((float) partialTicks));
	}
	
	/**
	 * Constructs a new Vector3 with the given x, y, z
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Constructs a new Vector3 with the given x, y, z
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3(Double x, Double y, Double z)
	{
		this(x.doubleValue(), y.doubleValue(), z.doubleValue());
	}
	
	/**
	 * Constructs a new Vector3 with all elements set to 0
	 */
	public Vector3()
	{
		this(0, 0, 0);
	}
	
	/**
	 * Constructs a new Vector3 that is a clone of the given vector3
	 *
	 * @param clone
	 */
	public Vector3(Vector3 clone)
	{
		this(clone.getX(), clone.getY(), clone.getZ());
	}
	
	/**
	 * Constructs a new Vector3 from the given Vector2 and z
	 *
	 * @param vector
	 * @param z
	 */
	public Vector3(Vector2 vector, double z)
	{
		this(vector.getX(), vector.getY(), z);
	}
	
	/**
	 * Constructs a new Vector3 from the given Vector2 and z set to 0
	 *
	 * @param vector
	 */
	public Vector3(Vector2 vector)
	{
		this(vector, 0);
	}

	/**
	 * Constructs a new Vector3 from Minecraft's 3D Vector class instance
	 *
	 * @param nativeVector Native Vector
	 */
	public Vector3(Vec3 nativeVector) {
		this(nativeVector.x(), nativeVector.y(), nativeVector.z());
	}
	
	public double getX()
	{
		return this.x;
	}
	
	public double getY()
	{
		return this.y;
	}
	
	public double getZ()
	{
		return this.z;
	}
	
	/**
	 * Adds two vectors
	 *
	 * @param that
	 * @return
	 */
	public Vector3 add(Vector3 that)
	{
		return Vector3.add(this, that);
	}
	
	/**
	 * Subtracts two vectors
	 *
	 * @param that
	 * @return
	 */
	public Vector3 subtract(Vector3 that)
	{
		return Vector3.subtract(this, that);
	}
	
	/**
	 * Scales by the scalar value
	 *
	 * @param scale
	 * @return
	 */
	public Vector3 scale(double scale)
	{
		return Vector3.scale(this, scale);
	}
	
	/**
	 * Takes the dot product of two vectors
	 *
	 * @param that
	 * @return
	 */
	public double dot(Vector3 that)
	{
		return Vector3.dot(this, that);
	}
	
	/**
	 * Takes the cross product of two vectors
	 *
	 * @param that
	 * @return
	 */
	public Vector3 cross(Vector3 that)
	{
		return Vector3.cross(this, that);
	}
	
	/**
	 * Returns a Vector2 object using the X and Z values of
	 * this Vector3. The x of this Vector3 becomes the x 
	 * of the Vector2, and the z of this Vector3 becomes the 
	 * y of the Vector2.
	 * 
	 * @return 
	 */
	public Vector2 toVector2()
	{
		return Vector3.toVector2(this);
	}
	
	/**
	 * Returns a Vector2m object using the X and Z values of
	 * this Vector3. The x of this Vector3 becomes the x 
	 * of the Vector2, and the z of this Vector3 becomes the 
	 * y of the Vector2m.
	 * 
	 * @return 
	 */
	public Vector2m toVector2m()
	{
		return Vector3.toVector2m(this);
	}
	
	/**
	 * Rounds the X, Y, and Z values of this Vector3 up to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector3 ceil()
	{
		return new Vector3(Math.ceil(this.x), Math.ceil(this.y), Math.ceil(this.z));
	}
	
	/**
	 * Rounds the X, Y, and Z values of this Vector3 down to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector3 floor()
	{
		return new Vector3(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
	}
	
	/**
	 * Rounds the X, Y, and Z values of this Vector3 to 
	 * the nearest integer value. 
	 * 
	 * @return 
	 */
	public Vector3 round()
	{
		return new Vector3(Math.round(this.x), Math.round(this.y), Math.round(this.z));
	}
	
	/**
	 * Sets the X, Y, and Z values of this Vector3 to their
	 * absolute value.
	 * 
	 * @return 
	 */
	public Vector3 abs()
	{
		return new Vector3(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
	}
	
	/**
	 * Gets the distance between this Vector3 and a given Vector3.
	 * 
	 * @param a
	 * @return 
	 */
	public double distance(Vector3 a)
	{
		return Vector3.distance(a, this);
	}
	
	/**
	 * Raises the X, Y, and Z values of this Vector3 to the given power.
	 * 
	 * @param power
	 * @return 
	 */
	public Vector3 pow(double power)
	{
		return Vector3.pow(this, power);
	}
	
	/**
	 * returns the squared length of the vector
	 *
	 * @return
	 */
	public double lengthSquared()
	{
		return Vector3.lengthSquared(this);
	}
	
	/**
	 * returns the length of this vector. Note: makes use of Math.sqrt and is
	 * not cached.
	 *
	 * @return
	 */
	public double length()
	{
		return Vector3.length(this);
	}
	
	/**
	 * Returns a fast approximation of this vector's length.
	 * 
	 * @return 
	 */
	public double fastLength()
	{
		return Vector3.fastLength(this);
	}
	
	/**
	 * returns the vector with a length of 1
	 *
	 * @return
	 */
	public Vector3 normalize()
	{
		return Vector3.normalize(this);
	}
	
	/**
	 * returns the vector as [x,y,z]
	 *
	 * @return
	 */
	public double[] toArray()
	{
		return Vector3.toArray(this);
	}
	
	/**
	 * Compares two Vector3s
	 */
	@Override
	public int compareTo(Vector3 o)
	{
		return Vector3.compareTo(this, o);
	}
	
	/**
	 * Checks if two Vector3s are equal
	 */
	@Override
	public boolean equals(Object o)
	{
		return Vector3.equals(this, o);
	}
	
	/**
	 * toString Override
	 */
	@Override
	public String toString()
	{
		return String.format("{ %f, %f, %f }", this.x, this.y, this.z);
	}
	
	/**
	 * Returns the length of the given vector. 
	 * 
	 * Note: Makes use of Math.sqrt and
	 * is not cached, so can be slow
	 *
	 * Also known as norm. ||a||
	 * 
	 * @param a
	 * @return
	 */
	public static double length(Vector3 a)
	{
		return Math.sqrt(lengthSquared(a));
	}
	
	/**
	 * Returns an approximate length of the given vector.
	 * 
	 * @param a
	 * @return 
	 */
	public static double fastLength(Vector3 a)
	{
		return Math.sqrt(lengthSquared(a));
	}
	
	/**
	 * returns the length squared to the given vector
	 *
	 * @param a
	 * @return
	 */
	public static double lengthSquared(Vector3 a)
	{
		return Vector3.dot(a, a);
	}
	
	/**
	 * Returns a new vector that is the given vector but length 1
	 *
	 * @param a
	 * @return
	 */
	public static Vector3 normalize(Vector3 a)
	{
		return Vector3.scale(a, (1.f / a.length()));
	}
	
	/**
	 * Creates a new vector that is A - B
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static Vector3 subtract(Vector3 a, Vector3 b)
	{
		return new Vector3(a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ());
	}
	
	/**
	 * Creates a new Vector that is A + B
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static Vector3 add(Vector3 a, Vector3 b)
	{
		return new Vector3(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getZ());
	}
	
	/**
	 * Creates a new vector that is A multiplied by the uniform scalar B
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static Vector3 scale(Vector3 a, double b)
	{
		return new Vector3(a.getX() * b, a.getY() * b, a.getZ() * b);
	}
	
	/**
	 * Returns the dot product of A and B
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static double dot(Vector3 a, Vector3 b)
	{
		return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
	}
	
	/**
	 * Creates a new Vector that is the A x B The Cross Product is the vector
	 * orthogonal to both A and B
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static Vector3 cross(Vector3 a, Vector3 b)
	{
		return new Vector3(a.getY() * b.getZ() - a.getZ() * b.getY(), a.getZ() * b.getX() - a.getX() * b.getZ(), a.getX() * b.getY() - a.getY() * b.getX());
	}
	
	/**
	 * Rounds the X, Y, and Z values of the given Vector3 up to 
	 * the nearest integer value. 
	 * 
	 * @param o Vector3 to use
	 * @return 
	 */
	public static Vector3 ceil(Vector3 o)
	{
		return new Vector3(Math.ceil(o.x), Math.ceil(o.y), Math.ceil(o.z));
	}
	
	/**
	 * Rounds the X, Y, and Z values of the given Vector3 down to 
	 * the nearest integer value. 
	 * 
	 * @param o Vector3 to use
	 * @return 
	 */
	public static Vector3 floor(Vector3 o)
	{
		return new Vector3(Math.floor(o.x), Math.floor(o.y), Math.floor(o.z));
	}
	
	/**
	 * Rounds the X, Y, and Z values of the given Vector3 to 
	 * the nearest integer value. 
	 * 
	 * @param o Vector3 to use
	 * @return 
	 */
	public static Vector3 round(Vector3 o)
	{
		return new Vector3(Math.round(o.x), Math.round(o.y), Math.round(o.z));
	}
	
	/**
	 * Sets the X, Y, and Z values of the given Vector3 to their
	 * absolute value.
	 * 
	 * @param o Vector3 to use
	 * @return 
	 */
	public static Vector3 abs(Vector3 o)
	{
		return new Vector3(Math.abs(o.x), Math.abs(o.y), Math.abs(o.z));
	}
	
	/**
	 * Returns a Vector3 containing the smallest X, Y, and Z values.
	 * 
	 * @param o1
	 * @param o2
	 * @return 
	 */
	public static Vector3 min(Vector3 o1, Vector3 o2)
	{
		return new Vector3(Math.min(o1.x, o2.x), Math.min(o1.y, o2.y), Math.min(o1.z, o2.z));
	}
	
	/**
	 * Returns a Vector3 containing the largest X, Y, and Z values.
	 * 
	 * @param o1
	 * @param o2
	 * @return 
	 */
	public static Vector3 max(Vector3 o1, Vector3 o2)
	{
		return new Vector3(Math.max(o1.x, o2.x), Math.max(o1.y, o2.y), Math.max(o1.z, o2.z));
	}
	
	/**
	 * Returns a Vector3 with random X, Y, and Z values (between 0 and 1)
	 * 
	 * @return 
	 */
	public static Vector3 rand()
	{
		return new Vector3(Math.random(), Math.random(), Math.random());
	}
	
	/**
	 * Gets the distance between two Vector3. 
	 * 
	 * @param a
	 * @param b
	 * @return 
	 */
	public static double distance(Vector3 a, Vector3 b)
	{
		double xzDist = Vector2.distance(a.toVector2(), b.toVector2());
		return Math.sqrt(Math.pow(xzDist, 2) + Math.pow(Math.abs(Vector3.subtract(a, b).getY()), 2));
	}
	
	/**
	 * Raises the X, Y, and Z values of a Vector3 to the given power.
	 * 
	 * @param o
	 * @param power
	 * @return 
	 */
	public static Vector3 pow(Vector3 o, double power)
	{
		return new Vector3(Math.pow(o.x, power), Math.pow(o.y, power), Math.pow(o.z, power));
	}
	
	/**
	 * Returns a Vector2 object using the X and Z values of
	 * the given Vector3. The x of the Vector3 becomes the x 
	 * of the Vector2, and the z of this Vector3 becomes the 
	 * y of the Vector2m.
	 * 
	 * @param o Vector3 object to use
	 * @return 
	 */
	public static Vector2 toVector2(Vector3 o)
	{
		return new Vector2(o.x, o.z);
	}
	
	/**
	 * Returns a Vector2m object using the X and Z values of
	 * the given Vector3. The x of the Vector3 becomes the x 
	 * of the Vector2m, and the z of this Vector3 becomes the 
	 * y of the Vector2m.
	 * 
	 * @param o Vector3 object to use
	 * @return 
	 */
	public static Vector2m toVector2m(Vector3 o)
	{
		return new Vector2m(o.x, o.z);
	}
	
	/**
	 * Returns a new double array that is {x, y, z}
	 *
	 * @param a
	 * @return
	 */
	public static double[] toArray(Vector3 a)
	{
		return new double[] { a.getX(), a.getY(), a.getZ() };
	}
	
	/**
	 * Compares two Vector3s
	 */
	public static int compareTo(Vector3 a, Vector3 b)
	{
		return (int)a.lengthSquared() - (int)b.lengthSquared();
	}
	
	/**
	 * Checks if two Vector3s are equal
	 */
	public static boolean equals(Object a, Object b)
	{
		if (!(a instanceof Vector3) || !(b instanceof Vector3))
		{
			return false;
		}
		if (a == b)
		{
			return true;
		}
		Vector3 x = (Vector3)a;
		Vector3 y = (Vector3)b;
		if (x.getX() == y.getX() && x.getY() == y.getY() && x.getZ() == y.getZ())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return (int)(this.x * this.y % this.z);
	}
}
