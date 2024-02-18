package com.shooting.main.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.shooting.main.model.Customer;
import com.shooting.main.model.Admin;
import com.shooting.main.model.User;
import com.shooting.main.repository.CustomerRepository;
import com.shooting.main.repository.AdminRepository;
import com.shooting.main.repository.UserRepository;

@CrossOrigin(origins = "http://localhost:3000/")
@RestController
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	CustomerRepository customerRepository;


	@Autowired
	AdminRepository adminRepository;

	@GetMapping("/user")
	public ResponseEntity<Object> getAllUser() {
		try {
			List<User> listUser = userRepository.findAll();
			List<Object> userListWithRoleData = new ArrayList<>();

			for (User user : listUser) {
				Map<String, Object> userWithRoleData = new HashMap<>();
				userWithRoleData.put("u_id", user.getU_id());
				userWithRoleData.put("username", user.getUsername());
				userWithRoleData.put("password", user.getPassword());
				userWithRoleData.put("role", user.getRole());
				Long userId = user.getU_id();

				if ("customer".equals(user.getRole())) {
					// Retrieve customer-specific data and add it to the response
					Customer customer = customerRepository.findByUserId(userId);
					if (customer != null) {
						userWithRoleData.put("id", customer.getC_id());
						userWithRoleData.put("fname", customer.getC_fname());
						userWithRoleData.put("lname", customer.getC_lname());
						userWithRoleData.put("c_tel", customer.getC_tel());
					}
				} else if ("admin".equals(user.getRole())) {
					// Retrieve admin-specific data and add it to the response
					Admin admin = adminRepository.findByUserId(userId);
					if (admin != null) {
						userWithRoleData.put("id", admin.getA_id());
						userWithRoleData.put("fname", admin.getA_fname());
						userWithRoleData.put("lname", admin.getA_lname());

					}
				}

				userListWithRoleData.add(userWithRoleData);
			}

			return new ResponseEntity<>(userListWithRoleData, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/user")
	public ResponseEntity<Object> addUser(@RequestBody Map<String, String> requestBody) {

		try {
			// Extract data for the User table
			String role = requestBody.get("role");
			if (role != null && (role.equals("customer") || role.equals("admin"))) {
				User newUser = new User();
				newUser.setUsername(requestBody.get("username"));
				newUser.setPassword(requestBody.get("password"));
				newUser.setRole(role);

				User savedUser = userRepository.save(newUser);

				if (role.equals("customer")) {
					Customer newCustomer = new Customer();
					newCustomer.setC_fname(requestBody.get("c_fname"));
					newCustomer.setC_lname(requestBody.get("c_lname"));
					newCustomer.setC_tel(requestBody.get("c_tel"));
					newCustomer.setUser(savedUser);

					Customer savedCustomer = customerRepository.save(newCustomer);

					Map<String, Object> response = new HashMap<>();
					response.put("Customer", savedCustomer);

					return new ResponseEntity<>(response, HttpStatus.CREATED);
				} else if (role.equals("admin")) {
					Admin newAdmin = new Admin();
					newAdmin.setA_fname(requestBody.get("a_fname"));
					newAdmin.setA_lname(requestBody.get("a_lname"));
					newAdmin.setUser(savedUser);

					Admin savedAdmin = adminRepository.save(newAdmin);
					Map<String, Object> response = new HashMap<>();
					response.put("Admin", savedAdmin);

					return new ResponseEntity<>(response, HttpStatus.CREATED);
				}
			} else {
				return new ResponseEntity<>("Invalid role. Role must be 'customer' or 'admin'.",
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	@GetMapping("/user/{userID}")
	public ResponseEntity<Object> getUserById(@PathVariable Long userID) {
		try {
			Optional<User> userFound = userRepository.findById(userID);

			if (userFound.isPresent()) {
				User user = userFound.get();
				Map<String, Object> userWithRoleData = new HashMap<>();
				userWithRoleData.put("u_id", user.getU_id());
				userWithRoleData.put("username", user.getUsername());
				userWithRoleData.put("role", user.getRole());
				Long userId = user.getU_id();

				if ("customer".equals(user.getRole())) {
					Customer customer = customerRepository.findByUserId(userId);
					if (customer != null) {
						userWithRoleData.put("id", customer.getC_id());
						userWithRoleData.put("fname", customer.getC_fname());
						userWithRoleData.put("lname", customer.getC_lname());
						userWithRoleData.put("c_tel", customer.getC_tel());
					}
				} else if ("admin".equals(user.getRole())) {
					Admin admin = adminRepository.findByUserId(userId);
					if (admin != null) {
						userWithRoleData.put("id", admin.getA_id());
						userWithRoleData.put("fname", admin.getA_fname());
						userWithRoleData.put("lname", admin.getA_lname());
					}
				}

				return new ResponseEntity<>(userWithRoleData, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("User Not Found.", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/user/{userID}")
	public ResponseEntity<Object> updateUserById(@PathVariable Long userID,
			@RequestBody Map<String, Object> updatedUserData) {
		try {
			Optional<User> userFound = userRepository.findById(userID);

			if (userFound.isPresent()) {
				User user = userFound.get();
				String role = user.getRole();
				Long userId = user.getU_id();

				if ("customer".equals(role)) {
					Customer customer = customerRepository.findByUserId(userId);
					if (customer != null) {
						// Update customer-specific data
						if (updatedUserData.containsKey("c_fname")) {
							customer.setC_fname((String) updatedUserData.get("c_fname"));
						}
						if (updatedUserData.containsKey("c_lname")) {
							customer.setC_lname((String) updatedUserData.get("c_lname"));
						}
						if (updatedUserData.containsKey("c_tel")) {
							customer.setC_tel((String) updatedUserData.get("c_tel"));
						}
						customerRepository.save(customer);
					}
				} else if ("admin".equals(role)) {
					Admin admin= adminRepository.findByUserId(userId);
					if (admin != null) {
						// Update admin-specific data
						if (updatedUserData.containsKey("a_fname")) {
							admin.setA_fname((String) updatedUserData.get("a_fname"));
						}
						if (updatedUserData.containsKey("a_lname")) {
							admin.setA_lname((String) updatedUserData.get("a_lname"));
						}
						adminRepository.save(admin);
					}
				}

				// You can also update common user data here
				if (updatedUserData.containsKey("username")) {
					user.setUsername((String) updatedUserData.get("username"));
					user.setPassword((String) updatedUserData.get("password"));
				}

				// Save the updated user data
				userRepository.save(user);

				return new ResponseEntity<>(updatedUserData, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("User Not Found.", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/user/{userID}")
	public ResponseEntity<Object> deleteUserById(@PathVariable Long userID) {
	    try {
	        Optional<User> userFound = userRepository.findById(userID);

	        if (userFound.isPresent()) {
	            User user = userFound.get();
	            String role = user.getRole();

	            if ("customer".equals(role)) {
	                // Delete the customer (if it exists)
	                Customer customer = customerRepository.findByUserId(userID);
	                if (customer != null) {
	                    customerRepository.delete(customer);
	                }
	            } else if ("admin".equals(role)) {
	                // Delete the admin (if it exists)
	            	Admin admin = adminRepository.findByUserId(userID);
	                if (admin != null) {
	                	adminRepository.delete(admin);
	                }
	            }

	            // Delete the user
	            userRepository.delete(user);

	            return new ResponseEntity<>("User, admin, or customer deleted successfully.", HttpStatus.OK);
	        } else {
	            return new ResponseEntity<>("User Not Found.", HttpStatus.NOT_FOUND);
	        }
	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	        return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PutMapping("/user/password/{userID}")
	public ResponseEntity<Object> updatePassword(@PathVariable Long userID, @RequestBody Map<String, String> newPasswordData) {
	    try {
	        Optional<User> userFound = userRepository.findById(userID);

	        if (userFound.isPresent()) {
	            User user = userFound.get();

	            // Check if the "password" key is present in the request body
	            if (newPasswordData.containsKey("password")) {
	                String newPassword = newPasswordData.get("password");
	                user.setPassword(newPassword); // Update the password field in the User entity
	                userRepository.save(user);

	                return new ResponseEntity<>("Password updated successfully.", HttpStatus.OK);
	            } else {
	                return new ResponseEntity<>("New password is missing in the request body.", HttpStatus.BAD_REQUEST);
	            }
	        } else {
	            return new ResponseEntity<>("User Not Found.", HttpStatus.NOT_FOUND);
	        }
	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	        return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}


	@PostMapping("/register")
	public ResponseEntity<Object> registerUser(@RequestBody Map<String, String> requestBody) {
		try {
			// Extract data for the User table
			User newUser = new User();
			newUser.setUsername(requestBody.get("username"));
			newUser.setPassword(requestBody.get("password"));
			newUser.setRole(requestBody.get("role"));

			User savedUser = userRepository.save(newUser);

			Customer newCustomer = new Customer();
			newCustomer.setC_fname(requestBody.get("c_fname"));
			newCustomer.setC_lname(requestBody.get("c_lname"));
			newCustomer.setC_tel(requestBody.get("c_tel"));
			newCustomer.setUser(savedUser);

			Customer savedCustomer = customerRepository.save(newCustomer);

			Map<String, Object> response = new HashMap<>();
//	        response.put("user", savedUser);
			response.put("customer", savedCustomer);

			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<Object> loginUser(@RequestBody User loginRequest) {
		try {
			Optional<User> userFound = userRepository.findByUsername(loginRequest.getUsername());

			if (userFound.isPresent() && userFound.get().getPassword().equals(loginRequest.getPassword())) {
				User loggedInUser = userFound.get();
				loggedInUser.setPassword(null); // Remove password for security

				String userRole = loggedInUser.getRole();
				Long userID = loggedInUser.getU_id();

				if ("customer".equals(userRole)) {
					Customer customer = customerRepository.findByUserId(userID);
					return new ResponseEntity<>(customer, HttpStatus.OK);
				} else if ("admin".equals(userRole)) {
					Admin admin = adminRepository.findByUserId(userID);
					return new ResponseEntity<>(admin, HttpStatus.OK);
				} else {
					return new ResponseEntity<>("Invalid role.", HttpStatus.UNAUTHORIZED);
				}
			} else {
				return new ResponseEntity<>("Invalid credentials.", HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}