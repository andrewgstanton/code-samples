<%@include file="nav_header.jsp" %>

<%@ page import="com.GenUtils"%>
<%@ page import="com.braintreegateway.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.lang.*"%>
<%@ page import="java.text.*"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.Date"%>

<div class="container">
            <div class="grid12 first">
                     <h2 class="title">Payment History</h2>
            </div>       
         <div class="grid5 first fleft">
		<table style='width:650px;'>
		<tr>
			<td><h4>Date Paid</h4></td>
			<td><h4>Amount</h4></td>
			<td><h4>Transaction ID</h4></td>
			<td><h4>Status</h4></td>
			<td><h4>CC NUm</h4></td>
		</tr>

<%
	String plan_type = "";
	Contact contact = new Contact(sc.getEditingSiteId(),"billing");
	String subscriptionId = contact.getBrainTreeSubscriptionId();
	ArrayList<Transaction> collection = null;

        String transactionId = "";
        String transactionAmount = "";
        String transactionCCNum = "";
        String transactionDate = "";
        String transactionStatus = "";
        Calendar cal = null;

        SimpleDateFormat prettyFormat = new SimpleDateFormat("MMM dd, yyyy");

	try {

		/* we are using the BrainTree directly to get the payment history */
		collection = (ArrayList<Transaction>) bt.updatePaymentHistory(subscriptionId);
        	if (collection != null) {
                              for (Transaction transaction : collection) {
                                        transactionId = transactionAmount  = transactionCCNum = transactionDate = transactionStatus =  "";
                                        transactionId = (String) transaction.getId();
                                        transactionAmount = (String) transaction.getAmount().toString();
                                        transactionCCNum = (String) transaction.getCreditCard().getMaskedNumber().toString();
                                        cal =  (Calendar) transaction.getUpdatedAt();
                                        transactionDate = prettyFormat.format(cal.getTime());
                                        transactionStatus = (String) transaction.getStatus().toString();

		                        out.print("<tr>\n");
                        			out.print("<td><p>"  +transactionDate + "</p></td>\n");
                        			out.print("<td><p>$" + transactionAmount + "</p></td>\n");
                        			out.print("<td><p>" + transactionId + "</p></td>\n");
                        			out.print("<td><p>" + transactionStatus + "</p></td>\n");
                       	 			out.print("<td><p>" + transactionCCNum + "</p></td>\n");
                        		out.print("</tr>\n");

                                }
		}
	} catch (Exception ex) {
		System.out.println("payments.jsp: " + ex.toString() + "\n");		
	} finally {

	}
%>

		</table>
                </div>
</div><!-- end container & content -->

<%@include file="widgets/knowledgebase_widget.jsp" %>
<%@include file="footer.jsp" %>
