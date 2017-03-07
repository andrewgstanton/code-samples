package com;

import java.math.BigDecimal;
import com.braintreegateway.*;

import java.sql.*;
import java.util.*;
import java.lang.*;
import java.text.*;
import java.util.Calendar;
import java.util.Date;


public class BrainTreePayment {

    // sandbox credentials

	// live credentials

BraintreeGateway gateway = new BraintreeGateway(
	Environment.PRODUCTION,
  	"XXXXXX", // merchant id
  	"XXXXX", 
  	"XXXX" 
);


    public BrainTreePayment() {
    }

	// create a new customer id for the siteId, if the customer id exists , return it

	public String createCustomer( String siteIdIn) {

		String braintreeCustomerId = "";

		if (!siteIdIn.equals("")) {
		// create contact object for customer
	        	Contact contact = new Contact(siteIdIn,"billing");
			Site site = new Site();

			String firstName = (contact.getFirstName() != null) ? contact.getFirstName() : "";
			String lastName = (contact.getLastName() != null) ? contact.getLastName() : "";
			String company = (contact.getFirmName() != null) ? contact.getFirmName() : "";
			String email = (contact.getEmail() != null) ? contact.getEmail() : "";
			String fax = (contact.getFax() != null) ? contact.getFax() : "";
			String phone = (contact.getTel() != null) ? contact.getTel() : "";
			String primaryDomain = (site.getFullPrimaryDomainFromSiteId(siteIdIn) != null) ? site.getFullPrimaryDomainFromSiteId(siteIdIn) : "";
			String ownerName = (site.getOwnerNameFromSiteId(siteIdIn) != null) ? site.getOwnerNameFromSiteId(siteIdIn) : "";
			if (email.equals("")) email = ownerName;

			//System.out.println("BrainTree.createCustomer: first name:" + firstName + "\n");
			//System.out.println("BrainTree.createCustomer: last name:" + lastName + "\n");
			//System.out.println("BrainTree.createCustomer: company:" +company + "\n");
			//System.out.println("BrainTree.createCustomer: email:" +email + "\n");
			//System.out.println("BrainTree.createCustomer: fax:" +fax + "\n");
			//System.out.println("BrainTree.createCustomer: phone:" +phone + "\n");
			//System.out.println("BrainTree.createCustomer: domain:" +primaryDomain + "\n");

			braintreeCustomerId = (contact.getBrainTreeCustomerId() != null) ? contact.getBrainTreeCustomerId() : "";
		//	System.out.println("BrainTree.createCustomer: bt cust id : " + braintreeCustomerId + "\n");

			if (braintreeCustomerId.equals("")) {
				//System.out.println("adding new customer id to contact ...\n");

				CustomerRequest request = new CustomerRequest().
    						firstName(firstName).
    						lastName(lastName).
    						company(company).
    						email(email).
    						fax(fax).
    						phone(phone).
    						website(primaryDomain);

				Result<Customer> result = gateway.customer().create(request);

				if (result.isSuccess()) {
					braintreeCustomerId = result.getTarget().getId().toString();
					contact.setBrainTreeCustomerId(braintreeCustomerId);
					contact.saveContact();
				}
			}

		}

		
		return braintreeCustomerId;

	}

	// update customer info on braintree (from our site)
	public boolean updateCustomer(String siteIdIn) {

		boolean resultStatus = false;

		// first create/or get the customer id from braintree
		String customerId = createCustomer(siteIdIn);
		
		// update fields in braintree
                if (!customerId.equals("")) {
                        Customer customer = gateway.customer().find(customerId);

                        Contact contact = new Contact(siteIdIn,"billing");
                        Site site = new Site();

                        String firstName = (contact.getFirstName() != null) ? contact.getFirstName() : "";
                        String lastName = (contact.getLastName() != null) ? contact.getLastName() : "";
                        String company = (contact.getFirmName() != null) ? contact.getFirmName() : "";
                        String email = (contact.getEmail() != null) ? contact.getEmail() : "";
                        String fax = (contact.getFax() != null) ? contact.getFax() : "";
                        String phone = (contact.getTel() != null) ? contact.getTel() : "";
                        String primaryDomain = (site.getFullPrimaryDomainFromSiteId(siteIdIn) != null) ? site.getFullPrimaryDomainFromSiteId(siteIdIn) : "";
                        String ownerName = (site.getOwnerNameFromSiteId(siteIdIn) != null) ? site.getOwnerNameFromSiteId(siteIdIn) : "";
                        if (email.equals("")) email = ownerName;

                        CustomerRequest request = new CustomerRequest().
                                                firstName(firstName).
                                                lastName(lastName).
                                                company(company).
                                                email(email).
                                                fax(fax).
                                                phone(phone).
                                                website(primaryDomain);

			Result<Customer> updateResult = gateway.customer().update(customerId, request);

			if (updateResult.isSuccess()) {
				resultStatus = true;
			}	
		}
		return resultStatus;
	}

	// returns true if the card is verified, false otherwise

	public boolean verifyCard(	String customerId, 
						String cvv,
						String ccNumber,
						String expMonth,
						String expYear ) {

		Result<CreditCard> result = null;
		boolean verifyResult = false;
		String ccToken = "";

//		System.out.println("BrainTreePayment.verifyCard: verifyResult before try: " + verifyResult + "\n");
		try {
			if (!customerId.equals("")) {
				Customer customer = gateway.customer().find(customerId);
				String firstName = customer.getFirstName();
				String lastName = customer.getLastName();
//				System.out.println("BrainTreePayment.verifyCard: after find: " + customerId + "\n");
//				System.out.println("BrainTreePayment.verifyCard: after find: " + firstName + "\n");
//				System.out.println("BrainTreePayment.verifyCard: after find: " + lastName + "\n");
//				System.out.println("BrainTreePayment.verifyCard: after find: " + cvv + "\n");
//				System.out.println("BrainTreePayment.verifyCard: after find: " + ccNumber + "\n");
//				System.out.println("BrainTreePayment.verifyCard: after find: " + expMonth + "\n");
//				System.out.println("BrainTreePayment.verifyCard: after find: " + expYear + "\n");

				CreditCardRequest request = new CreditCardRequest().
    							customerId(customerId).
   				 			cardholderName(firstName + " " + lastName).
    							cvv(cvv).
    							number(ccNumber).
    							expirationDate(expMonth + "/" + expYear).
    							options().
       								verifyCard(true).
								makeDefault(true).
        						done();

				result = gateway.creditCard().create(request);
				if (result.isSuccess()) {
					verifyResult = true;
				} else {
        				CreditCardVerification verification = result.getCreditCardVerification();
        				System.out.println("verify card status = " + verification.getStatus() + "\n");
        				// processor_declined
        				System.out.println("response code = " + verification.getProcessorResponseCode() + "\n");
        				// 2000
       					System.out.println("response text = " + verification.getProcessorResponseText() + "\n");
        				// Do Not Honor
				}
                        	ccToken = getCCToken(customerId);
 //                       	if (!ccToken.equals("")) gateway.creditCard().delete(ccToken);
			}
		} catch (Exception ex) {
			System.out.println("BrainTreePayment:verifyCard: exception: " + ex.toString() + "\n");
		} finally {
		//		System.out.println("BrainTreePayment.verifyCard after try: customerId: " + customerId + "\n");
		//		System.out.println("BrainTreePayment.verifyCard after try: verifyResult: " + verifyResult + "\n");
			System.out.println("BrainTreePayment.verifyCard after try: ccToken: " + ccToken + "\n");
			if (!ccToken.equals("")) gateway.creditCard().delete(ccToken);
		}
		return verifyResult;
	}


	// updates the primary cc num for the the customer
	// it will return true or false
	
	public boolean updateVault(String customerId,
				 	String cvv,
					String ccNumber,
					String expMonth,
					String expYear) {

                boolean updateResult = false;
                boolean verifyResult = false;

		String ccToken = "";

                Result<CreditCard> updateCCResult = null;
                try {
                        verifyResult = verifyCard(customerId,
                                                        cvv,
                                                        ccNumber,
                                                        expMonth,
                                                        expYear);

                        if (verifyResult) {

                                Customer customer = gateway.customer().find(customerId);
                                String firstName = customer.getFirstName();
                                String lastName = customer.getLastName();

				// remove the current card
				ccToken = getCCToken(customerId);
				if (!ccToken.equals("")) gateway.creditCard().delete(ccToken);

                                CreditCardRequest request = new CreditCardRequest().
                                                        customerId(customerId).
                                                        cardholderName(firstName + " " + lastName).
                                                        cvv(cvv).
                                                        number(ccNumber).
                                                        expirationDate(expMonth + "/" + expYear).
                                                        options().
                                                                makeDefault(true).
                                                        done();

                                updateCCResult = gateway.creditCard().create(request);
                                if (updateCCResult.isSuccess()) {
					updateResult = true;	
				}
			}
		} catch (Exception ex) {
                        System.out.println("BrainTreePayment.updateVault: " + ex.toString());
		}

		return updateResult;
	}

	// associates the cc num w/ the customer
	// it will return the token associated w/ the credit card
	
	public String addToVault(	String customerId,
					String cvv,
					String ccNumber,
					String expMonth,
					String expYear) {

		String ccToken = "";

		boolean verifyResult = false;

		Result<CreditCard> addResult = null;
		try {
	//		System.out.println("BrainTreePayment:addToVault: customerId before verify: " + customerId + "\n");
			verifyResult = verifyCard(customerId,
							cvv,
							ccNumber,
							expMonth,
							expYear);

			if (verifyResult) {

	//			System.out.println("BrainTreePayment:addToVault: customerId after verify: " + customerId + "\n");

                        	Customer customer = gateway.customer().find(customerId);
                        	String firstName = customer.getFirstName();
                        	String lastName = customer.getLastName();

//				System.out.println("BrainTreePayment:addToVault: firstName : " + firstName + "\n");
//				System.out.println("BrainTreePayment:addToVault: lastName : " + firstName + "\n");
				CreditCardRequest request = new CreditCardRequest().
    							customerId(customerId).
							cardholderName(firstName + " " + lastName).
							cvv(cvv).
    							number(ccNumber).
    							expirationDate(expMonth + "/" + expYear).
							options().
								makeDefault(true).
							done();

				addResult = gateway.creditCard().create(request);
				if (addResult.isSuccess()) {
					ccToken = getCCToken(customerId);
				}	
			}
		} catch (Exception ex) {
			System.out.println("BrainTreePayment.addToVault: " + ex.toString());
		}

		return ccToken;
	}		

	// returns a cctoken for the first cc associated w/ this customer id on braintree
	public String getCCToken(String customerId) {

		String ccToken = "";

		try {	
			if (!customerId.equals("")) {
				Customer customer = gateway.customer().find(customerId);
				if (customer != null) {
					if (customer.getCreditCards() != null) {
						if (customer.getCreditCards().get(0) != null) {
							if (customer.getCreditCards().get(0).getToken() != null) {
								ccToken = customer.getCreditCards().get(0).getToken();
							}
						}
					}
				}
			}		
		}  catch (Exception ex) {
			System.out.println("getCCToken: exception: " + ex.toString());
		}

		return ccToken;
	}	
	

	public Result<Subscription> addSubscription(String customerId, String planId) {

		Result<Subscription> subscriptionResult = null;
		boolean subscriptionAdded = false;
		String ccToken = getCCToken(customerId);
		if (!ccToken.equals("")) {
			SubscriptionRequest request = new SubscriptionRequest().
    			paymentMethodToken(ccToken).
    			planId(planId);
			subscriptionResult = gateway.subscription().create(request);
		}
		return subscriptionResult;
	}			

	
	/* add a subscription for the siteId, if the subscription has already been added return the string and do nothing */
	public String addBrainTreeSubscription(String siteIdIn, 
						String wcPlanId,
						String cvv,
						String ccNumber,
						String expMonth,
						String expYear) {


		// first update (or create the customer from braintree)
		boolean updateResult = updateCustomer(siteIdIn);
		Contact	contact = new Contact(siteIdIn, "billing");
		
		String ccToken = "";
		String subscriptionId = "";
		String customerId = contact.getBrainTreeCustomerId();

		// if customerId, see if there is a credit card token, use that
		if (!customerId.equals("")) {

	//		System.out.println("BrainTreePayment:addBrainTreeSubscription: customer id: " + customerId + "\n");

			ccToken = getCCToken(customerId);
			System.out.println("BrainTreePayment:addBrainTreeSubscription: ccToken: " + ccToken + "\n");
			// if there is no token create a token for the subscription
			if (ccToken.equals("")) {
				ccToken = addToVault(customerId, cvv, ccNumber, expMonth, expYear);
//				System.out.println("BrainTreePayment:addBrainTreeSubscription: addToVault: " + ccToken + "\n");
			}
			// if we have a token now, create the subscription
			if (!ccToken.equals("")) {
				subscriptionId = contact.getBrainTreeSubscriptionId();

				// if no subscription, set it up
				if (subscriptionId.equals("")) {
					// get the brain tree payment id
					String btPlanId = getBrainTreePlanIdFromWCPlanId(wcPlanId);
					Result<Subscription> result = addSubscription(customerId, btPlanId);

	                                if (result.isSuccess()) {
						// get the subscription id
						Subscription subscription = result.getTarget();
						subscriptionId = subscription.getId().toString();
						//System.out.println("subscription id: " + subscriptionId + "\n");
               		                        contact.setBrainTreeSubscriptionId(subscriptionId);
               		                        contact.saveContact();

						// activate account
						setActive(siteIdIn);

						// set plan
						setPlan(siteIdIn, wcPlanId);
                       		         }
				}
			}
			
		}

		contact = null;

		return subscriptionId;
	
	}

	private String getBrainTreePlanIdFromWCPlanId(String wcPlanId) {

	        String sql = "";
        	ResultSet rs = null;
		String btPlanId = "";
       
		if (!wcPlanId.equals("")) {
			try {
               			sql = "SELECT braintree_plan_id FROM webchalet_account_types ";
                		sql += " WHERE id = " + wcPlanId;
                		rs = MysqlQuery.runQuery(sql);
               	 		while (rs.next()) {
					btPlanId = (rs.getString("braintree_plan_id") != null) ? rs.getString("braintree_plan_id") : "";
				}
			} catch (Exception ex) {
				System.out.println("exception in BrainTreePayent:getBrainTreePlanIdFromWCPlanId: " + ex.toString());
			}
		}
		return btPlanId;		
	}

	// cannot access plans from the api for some reason there is no plan.find()

	private String getPlanNameFromBrainTreePlanId(String btPlanId) {
                String sql = "";
                ResultSet rs = null;
                String planName = "";

                if (!btPlanId.equals("")) {
                        try {
                                sql = "SELECT name FROM webchalet_account_types ";
                                sql += " WHERE braintree_plan_id = " + btPlanId;
                                rs = MysqlQuery.runQuery(sql);
                                while (rs.next()) {
                                       planName = (rs.getString("name") != null) ? rs.getString("name") : "";
                                }
                        } catch (Exception ex) {
                                System.out.println("exception in BrainTreePayent:getPlanNameFromBrainTreePlanId: " + ex.toString());
                        }
                }
                return planName;
	}

	private boolean setActive(String siteIdIn) {
		boolean result = false;
                if (!siteIdIn.equals("")) {
                        try {

                                // update sites set trial account to false
                                String sql = "UPDATE sites set is_trial_account = 0 WHERE id = " + siteIdIn;
                                MysqlQuery.runUpdate(sql);
                                result = true;
                        } catch (Exception ex) {
                                System.out.println("BrainTreePayment: setActive: Exception: " + ex.toString());
                        }
                }
		return result;
	}

	private boolean setPlan(String siteIdIn, String wcPlanIdIn) {
                boolean result = false;
                if (!siteIdIn.equals("") && !wcPlanIdIn.equals("") ) {
                        try {
                                // update sites set trial account to false
                                String sql = "UPDATE sites set account_plan = " + wcPlanIdIn + "  WHERE id = " + siteIdIn;
                                MysqlQuery.runUpdate(sql);
                                result = true;
                        } catch (Exception ex) {
                                System.out.println("BrainTreePayment: setPlan: Exception: " + ex.toString());
                        }
                }
                return result;
	}

    public Result<Transaction> processRequest (String amount,
                                String ccNumber,
                                String expMonth,
                                String expYear ) {

   //    System.out.println("process Request called ...\n");
        TransactionRequest request = new TransactionRequest().
            				amount(new BigDecimal(amount)).
            				creditCard().
                			number(ccNumber).
                			expirationDate(expMonth + "/" + expYear).
                			done();

        Result<Transaction> result = gateway.transaction().sale(request);
/*
        if (result.isSuccess()) {
            Transaction transaction = result.getTarget();
            System.out.println("Success!: " + transaction.getId());
        } else if (result.getTransaction() != null) {
            System.out.println("Message: " + result.getMessage());
            Transaction transaction = result.getTransaction();
            System.out.println("Error processing transaction:");
            System.out.println("  Status: " + transaction.getStatus());
            System.out.println("  Code: " + transaction.getProcessorResponseCode());
            System.out.println("  Text: " + transaction.getProcessorResponseText());
        } else {
            System.out.println("Message: " + result.getMessage());
            for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
                System.out.println("Attribute: " + error.getAttribute());
                System.out.println("  Code: " + error.getCode());
                System.out.println("  Message: " + error.getMessage());
            }
        }
*/

        return result;
   }


   public Hashtable makePayment(	String amount, 
				String ccNumber,
				String expMonth,
				String expYear,
				String siteId,
				String dueDate,
				String currentPlan) {


    // save payment to payment history
	String sql = "";
	ResultSet rs = null;
	String date_posted = "";

	String cc_card_suffix = "";
	boolean payment_result = false; 
	String transactionId = "";

	String transaction_message = "";

	Hashtable bt_result = new Hashtable();

	if (!ccNumber.equals("")) {	
		date_posted = GenUtils.mySQLTimeStamp();
		cc_card_suffix = ccNumber.substring(ccNumber.length()-4);		

       		Result<Transaction> result =processRequest(amount, ccNumber, expMonth, expYear);
       		if (result.isSuccess()) {
       	 		Transaction transaction = result.getTarget();
       			transactionId = (String) transaction.getId() ;
       		} else if (result.getTransaction() != null) {
       			System.out.println("Message: " + result.getMessage());
			transaction_message = result.getMessage().toString();
	
       			Transaction transaction = result.getTransaction();
        		System.out.println("Error processing transaction:");
           	 	System.out.println("  Status: " + transaction.getStatus());
            		System.out.println("  Code: " + transaction.getProcessorResponseCode());
            		System.out.println("  Text: " + transaction.getProcessorResponseText());
        	} else {
            		System.out.println("Message: " + result.getMessage());
			transaction_message = result.getMessage().toString();
            		for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
               			System.out.println("Attribute: " + error.getAttribute());
               			System.out.println("  Code: " + error.getCode());
               			System.out.println("  Message: " + error.getMessage());
            		}
		}
		if (result.isSuccess()) {	
			try {
				if (!currentPlan.equals("")) {
					sql = "INSERT INTO payment_history (date_posted, due_date, amount, site_id, account_plan, transaction_id, cc_num_suffix) ";
					sql += " VALUES ('" + date_posted + "','" + dueDate + "'," +  amount + "," + siteId + "," + currentPlan + ",'" + transactionId + "','" + cc_card_suffix + "')";
				} else {
					sql = "INSERT INTO payment_history (date_posted, due_date, amount, site_id, transaction_id, cc_num_suffix) ";
					sql += " VALUES ('" + date_posted + "','" + dueDate + "'," +  amount + "," + siteId + ",'" + transactionId + "','" + cc_card_suffix + "')";
				}
				MysqlQuery.runUpdate(sql);
				System.out.println("BrainTreePayment.makePayment: " + sql + "\n");

				// update sites set trial account to false
				sql = "UPDATE sites set is_trial_account = 0 WHERE id = " + siteId;
				MysqlQuery.runUpdate(sql);
				payment_result = true;
			} catch (Exception ex) {
				System.out.println("BrainTreePayment: Exception: " + ex.toString());
			}
		}
	}

	bt_result.put("status", payment_result);
	if (payment_result) {
		bt_result.put("message", "Success - Transaction ID: " + transactionId);
	} else {
		bt_result.put("message", "Failure - " + transaction_message);
	}
	return bt_result;
   }

   public String calcNextPayment(String siteIdIn) {

	String current_due_date = "";
	String subscriptionId = "";
	Subscription subscription = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m:s");
	Calendar  nextBillingDate = null;
	if (!siteIdIn.equals("")) {
		try {
                        Contact contact = new Contact(siteIdIn,"billing");
			subscriptionId = contact.getBrainTreeSubscriptionId();
			if (!subscriptionId.equals("")) {
                                subscription = gateway.subscription().find(subscriptionId);
				nextBillingDate = (Calendar) subscription.getNextBillingDate();
				current_due_date = (String) sdf.format(nextBillingDate.getTime());	
			} else { 	
                        	Calendar cal = Calendar.getInstance();
                        	Date today = cal.getTime();
				current_due_date = (String) sdf.format(today);
			}
		} catch (Exception ex) {
			System.out.println("BrainTreePayment.checkNextPayment: " + ex.toString());
		}
	}		
	return current_due_date;
   }



	// check if this site is currently using braintree for payments

	public boolean checkIfUsingBrainTree(String siteIdIn) {

		String use_braintree = "0";
	        String sql = "";
        	ResultSet rs = null;

		if (!siteIdIn.equals("")) {
       	 		try {
                		sql = "SELECT s.use_braintree FROM sites s WHERE s.id = " + siteIdIn;
                		rs = MysqlQuery.runQuery(sql);
                		while (rs.next()) {
					use_braintree = (rs.getString("use_braintree") != null) ? rs.getString("use_braintree") : "0";
				}
			} catch (Exception ex) {
				System.out.println("BrainTreePayment.checkifUsingBraintree: " + ex.toString());
			} finally {
				if (rs != null) MysqlQuery.freeResources(rs);
				rs = null;
			}
		}
		boolean use_braintree_result = (use_braintree.equals("1")) ? true : false;
		return use_braintree_result;
	}

	// set brain tree setting for site

	public void setUsingBrainTree(String siteIdIn, String useBrainTreeIn) {

		String sql = "";

		if (!siteIdIn.equals("") && !useBrainTreeIn.equals("")) {
			try {
				sql = "UPDATE sites set use_braintree = " + useBrainTreeIn + " WHERE id = " + siteIdIn;
				System.out.println("BrainTreePayment.setUsingBrainTree: " + sql + "\n");

				MysqlQuery.runUpdate(sql);
			} catch (Exception ex) {
				System.out.println("BrainTreePayment.setUsingBrainTree: " + ex.toString());
			}
		} else if (!useBrainTreeIn.equals("")) {
                        try {
                                sql = "UPDATE sites set use_braintree = " + useBrainTreeIn + " WHERE 1 ";
                                System.out.println("BrainTreePayment.setUsingBrainTree: " + sql + "\n");
                                MysqlQuery.runUpdate(sql);
                        } catch (Exception ex) {
                                System.out.println("BrainTreePayment.setUsingBrainTree: " + ex.toString());
                        }
		}

	}


	public ArrayList<Transaction>  updatePaymentHistory(String subscriptionIdIn) {
	
		Subscription subscription = null;
		ArrayList<Transaction> collection = null;
		String transactionId = "";
		String transactionAmount = "";
		String transactionCCNum = "";
		
		if (!subscriptionIdIn.equals("")) {
			try {
				subscription = gateway.subscription().find(subscriptionIdIn);
				collection = (ArrayList<Transaction>) subscription.getTransactions();
/*				for (Transaction transaction : collection) {
					transactionId = transactionAmount  = "";
					transactionId = (String) transaction.getId();
					transactionAmount = (String) transaction.getAmount().toString();
					transactionCCNum = (String) transaction.getCreditCard().getMaskedNumber().toString();
				
    					System.out.println("subscription: " + subscriptionIdIn + ": id:" + transactionId + "\n");
    					System.out.println("subscription: " + subscriptionIdIn + ": amount:  " + transactionAmount + "\n");
    					System.out.println("subscription: " + subscriptionIdIn + ": cc num:  " + transactionCCNum + "\n");
				}

*/
			} catch (Exception ex) {
				System.out.println("BrainTreePayment.getPaymentHistory: " + ex.toString());
			}
		}
		return collection;
	}


	// returns the default cc num associated w/ this acocunt
	public String getDefaultCCNumber(String customerIdIn) {

		String defaultCCNum = "";
		String ccToken = "";
		CreditCard cc = null;
		if (!customerIdIn.equals("")) {
			try {
                                ccToken = getCCToken(customerIdIn);
                              	if (!ccToken.equals("")) {
					cc = gateway.creditCard().find(ccToken);
					defaultCCNum = (String) cc.getMaskedNumber();
				}
			} catch (Exception ex) {
				System.out.println("BrainTreePayment.getDefaultCCNumber: " + ex.toString());
			}
		}

		return defaultCCNum;
	}


	public String getCurrentPlanName(String subscriptionIdIn) {

                String planName = "";
                String planId = "";
		Subscription subscription = null;
                if (!subscriptionIdIn.equals("")) {
                        try {
                                subscription = gateway.subscription().find(subscriptionIdIn);
				planId = (String) subscription.getPlanId();
				planName = getPlanNameFromBrainTreePlanId(planId);
                        } catch (Exception ex) {
                                System.out.println("BrainTreePayment.getCurrentPlanName: " + ex.toString());
                        }
                }
                return planName;
	}	
}

