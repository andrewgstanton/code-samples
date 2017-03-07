<%@ page import="com.BrainTreePayment"%>
<%@ page import="com.Contact"%>
<%@ page import="com.GenUtils"%>
<%

        // we are using subscriptions now in braintree
	String statusMsg = "";
	String jsonResult = "{'status':'-1','msg':'no data'}\n";

	String cardNumber = (request.getParameter("card_number") != null) ? request.getParameter("card_number") : "";
	String accountPlan = (request.getParameter("account_plan") != null) ? request.getParameter("account_plan") : "";
	String cvv = (request.getParameter("cvv") != null) ? request.getParameter("cvv") : "";
	String expMonth= (request.getParameter("exp_month") != null) ? request.getParameter("exp_month") : "";
	String expYear= (request.getParameter("exp_year") != null) ? request.getParameter("exp_year") : "";
	String siteId = (request.getParameter("site_id") != null) ? request.getParameter("site_id") : "";

	String subscriptionId = "";
	String nextPayment = "";
	String defaultCCNumber = "";	
	String customerId = "";

        if (!cardNumber.equals("") && cardNumber.length() >= 15 ) {

		BrainTreePayment bt = new BrainTreePayment();
                subscriptionId = bt.addBrainTreeSubscription(siteId, accountPlan, cvv, cardNumber, expMonth, expYear);
                if (!subscriptionId.equals("")) {
               		nextPayment = GenUtils.prettyFormat(bt.calcNextPayment(siteId));
			Contact contact = new Contact(siteId,"billing");
			customerId = contact.getBrainTreeCustomerId();
                        defaultCCNumber = bt.getDefaultCCNumber(customerId);
			statusMsg = "Thank you! Payment received!";
			jsonResult = "{'status':'1',";
			jsonResult += "'msg':'" + statusMsg + "',";
			jsonResult += "'subscription_id':'" + subscriptionId + "',";
			jsonResult += "'default_cc_number':'" + defaultCCNumber + "',";
			jsonResult += "'next_payment':'" + nextPayment + "'}\n";
                } else {
			jsonResult = "{'status':'-1','msg':'There was an error with your card, please try again'}\n";
		}
        } else if (cardNumber.equals("")) {
                statusMsg = "Please enter a credit card number.";
		jsonResult = "{'status':'-1','msg':'" + statusMsg + "'}\n";
        } else if (cardNumber.length() <15) {
                statusMsg = "Please enter a valid credit card number.";
		jsonResult = "{'status':'-1','msg':'" + statusMsg + "'}\n";
	}
	out.print(jsonResult);

%>

