
public class CoinFlippingTest {
	public static void main(String[] args) {
		CoinFlippingUser a, b; 
		a = new CoinFlippingUser("Anna"); 
		b = new CoinFlippingUser("Bob"); 
		System.out.println("Winner: " + a.flipCoin(b)); 
	}
}
