/**
 *
 * $Id$
 */
package mteach;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Professor</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link mteach.Professor#getLastName <em>Last Name</em>}</li>
 *   <li>{@link mteach.Professor#getFirstName <em>First Name</em>}</li>
 *   <li>{@link mteach.Professor#getTeachedCourses <em>Teached Courses</em>}</li>
 * </ul>
 * </p>
 *
 * @see mteach.MteachPackage#getProfessor()
 * @model
 * @extends INeo4emfObject
 * @generated
 */
public interface Professor extends INeo4emfObject {

	/**
	 * Returns the value of the '<em><b>Last Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 *XX6a
	 * <p>
	 * If the meaning of the '<em>Last Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last Name</em>' attribute.
	 * @see #setLastName(String)
	 * @see mteach.MteachPackage#getProfessor_LastName()
	 * @model required="true"
	 * @generated
	 */
	String getLastName();
	/**
	 * Sets the value of the '{@link mteach.Professor#getLastName <em>Last Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 *YY1
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Name</em>' attribute.
	 * @see #getLastName()
	 * @generated
	 */
	void setLastName(String value);
	/**
	 * Returns the value of the '<em><b>First Name</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 *XX6a
	 * <p>
	 * If the meaning of the '<em>First Name</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First Name</em>' attribute list.
	 * @see mteach.MteachPackage#getProfessor_FirstName()
	 * @model required="true"
	 * @generated
	 */
	EList<String> getFirstName();
	/**
	 * Returns the value of the '<em><b>Teached Courses</b></em>' reference list.
	 * The list contents are of type {@link mteach.Course}.
	 * It is bidirectional and its opposite is '{@link mteach.Course#getProfessor <em>Professor</em>}'.
	 * <!-- begin-user-doc -->
	 *XX6a
	 * <p>
	 * If the meaning of the '<em>Teached Courses</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Teached Courses</em>' reference list.
	 * @see mteach.MteachPackage#getProfessor_TeachedCourses()
	 * @see mteach.Course#getProfessor
	 * @model opposite="professor" ordered="false"
	 * @generated
	 */
	EList<Course> getTeachedCourses();




} // Professor
