# QUICK HOW-TO-RUN EXAMPLE

```
bash

cd meta-agent
mvn clean install
cd meta-agent-service
mvn spring-boot:run
.....................
on shell prompt enter:

ask --intent "find french restaurant near upper east side manhattan"

```
OUTPUT:

18:04:20.022 [main] INFO  RestaurantFinder - Here is a comparison summary of the menus fetched from the five restaurants:

1. Restaurant Daniel:
- Cuisine: French, Vegetarian, Cocktails
- Menu Highlights:
  - Appetizers: Oysters, Foie Gras, Maine Lobster Salad, Tuna, Black Truffle cuisines.
  - Mains: Black Sea Bass, Yellowfin Tuna, Quebec Suckling Pig, Wagyu Beef.
  - Cheese offerings from around the world.
  - Dessert specialties including Apple Vacherin and Chocolate Mousse.
  - Tasting menu priced at $225.
  - Wide beverage selection including cocktails, champagne, and specialty cocktails.

2. Restaurant Table d'Hôte:
- Cuisine: French Bistro
- Menu Highlights:
  - Lunch and Dinner sections featuring Soup du Jour, Mixed Greens, Omelets, Grilled Chicken.
  - Brunch offerings like Croque Monsieur, French Toast, Eggs Benedict.
  - Main courses include Veal Porterhouse, Rack of Lamb, Duck Confit, Roasted Salmon.
  - Casual bistro style with moderately priced dishes typically ranging $7-$49.

3. Cafe Luxembourg:
- Cuisine: French Bistro, Coffee & Tea
- Menu Highlights:
  - Brunch items: Eggs any style, Omelets with choice of ingredients, Avocado Toast, Steak Frites.
  - Lunch: Oysters, Soups, Salads, Sandwiches including Lobster Roll and LuxemBurger.
  - Dinner includes classics like French Onion Soup, Duck Liver Mousse, Rack of Lamb.
  - Variety of sides and baked goods.
  - Desserts include Creme Brulee and Profiteroles.
  - Pricing mostly in the $9-$40 range with casual to mid-range bistro offerings.

4. Cafe D'Alsace:
- Cuisine: French, German Bistronomy
- Menu Highlights:
  - Special Thanksgiving Day menu with traditional turkey and pumpkin pie.
  - Bistro favorites such as French Onion Soup, Duck Terrine, Choucroute Garnie.
  - Main dishes feature Alsatian specialties, artisan sausages, and steak frites.
  - Weekend brunch featuring classic dishes like Croque Madame, Smoked Salmon Board.
  - Desserts include Creme Brulee, Cheesecake, and Chocolate Tart.
  - Prices span from $5 to $54, emphasizing traditional French-German bistro fare.

5. Nougatine and Terrace at Jean Georges:
- Cuisine: Asian Fusion, French, Cocktails
- Menu Highlights:
  - Diverse selections with a Tasting Menu for Two costing $392.
  - Appetizers include Foie Gras, Tuna Tartare, Crispy Trout Sushi, and Butternut Squash Soup.
  - Entrees feature Organic Turkey (seasonal), Lobster Burger, Pan Roasted Lamb Chops, and many seafood dishes.
  - Sides include Potato Puree, French Fries, and Roasted Mushrooms.
  - Decadent desserts including Butterscotch Pudding and Chocolate Pudding Cake.
  - Extensive and luxurious offering with higher price points.

Summary:
- Daniel stands out for haute French cuisine with luxurious tasting menus and high-end ingredients.
- Table d'Hôte and Cafe Luxembourg offer classic French bistro fare with approachable pricing.
- Cafe D'Alsace uniquely blends French and German influences with a focus on Alsatian specialties.
- Nougatine at Jean Georges presents a refined fusion of Asian-French cuisine with elegant tasting menus and premium prices.
- All menus exhibit rich French culinary traditions, but vary in formality, price range, and additional culinary influences (German, Asian fusion).

18:04:20.022 [main] INFO  RestaurantFinder - Here is a comparison summary of the menus fetched from the five restaurants:

1. Restaurant Daniel:
- Cuisine: French, Vegetarian, Cocktails
- Menu Highlights:
  - Appetizers: Oysters, Foie Gras, Maine Lobster Salad, Tuna, Black Truffle cuisines.
  - Mains: Black Sea Bass, Yellowfin Tuna, Quebec Suckling Pig, Wagyu Beef.
  - Cheese offerings from around the world.
  - Dessert specialties including Apple Vacherin and Chocolate Mousse.
  - Tasting menu priced at $225.
  - Wide beverage selection including cocktails, champagne, and specialty cocktails.

2. Restaurant Table d'Hôte:
- Cuisine: French Bistro
- Menu Highlights:
  - Lunch and Dinner sections featuring Soup du Jour, Mixed Greens, Omelets, Grilled Chicken.
  - Brunch offerings like Croque Monsieur, French Toast, Eggs Benedict.
  - Main courses include Veal Porterhouse, Rack of Lamb, Duck Confit, Roasted Salmon.
  - Casual bistro style with moderately priced dishes typically ranging $7-$49.

3. Cafe Luxembourg:
- Cuisine: French Bistro, Coffee & Tea
- Menu Highlights:
  - Brunch items: Eggs any style, Omelets with choice of ingredients, Avocado Toast, Steak Frites.
  - Lunch: Oysters, Soups, Salads, Sandwiches including Lobster Roll and LuxemBurger.
  - Dinner includes classics like French Onion Soup, Duck Liver Mousse, Rack of Lamb.
  - Variety of sides and baked goods.
  - Desserts include Creme Brulee and Profiteroles.
  - Pricing mostly in the $9-$40 range with casual to mid-range bistro offerings.

4. Cafe D'Alsace:
- Cuisine: French, German Bistronomy
- Menu Highlights:
  - Special Thanksgiving Day menu with traditional turkey and pumpkin pie.
  - Bistro favorites such as French Onion Soup, Duck Terrine, Choucroute Garnie.
  - Main dishes feature Alsatian specialties, artisan sausages, and steak frites.
  - Weekend brunch featuring classic dishes like Croque Madame, Smoked Salmon Board.
  - Desserts include Creme Brulee, Cheesecake, and Chocolate Tart.
  - Prices span from $5 to $54, emphasizing traditional French-German bistro fare.

5. Nougatine and Terrace at Jean Georges:
- Cuisine: Asian Fusion, French, Cocktails
- Menu Highlights:
  - Diverse selections with a Tasting Menu for Two costing $392.
  - Appetizers include Foie Gras, Tuna Tartare, Crispy Trout Sushi, and Butternut Squash Soup.
  - Entrees feature Organic Turkey (seasonal), Lobster Burger, Pan Roasted Lamb Chops, and many seafood dishes.
  - Sides include Potato Puree, French Fries, and Roasted Mushrooms.
  - Decadent desserts including Butterscotch Pudding and Chocolate Pudding Cake.
  - Extensive and luxurious offering with higher price points.

Summary:
- Daniel stands out for haute French cuisine with luxurious tasting menus and high-end ingredients.
- Table d'Hôte and Cafe Luxembourg offer classic French bistro fare with approachable pricing.
- Cafe D'Alsace uniquely blends French and German influences with a focus on Alsatian specialties.
- Nougatine at Jean Georges presents a refined fusion of Asian-French cuisine with elegant tasting menus and premium prices.
- All menus exhibit rich French culinary traditions, but vary in formality, price range, and additional culinary influences (German, Asian fusion).


Agent was created by shell commands:
```
design --intent "restaurant finder. find restaurant near me. use proper typed Restaurant entity. get links to restaurant menus. compare menus for  top restaurants. name as restaurantFinder"
(that is your first commabd to generate agent)
gen-tools
```

See section "Practical Guide in README"
mvn clean install - after generated agent compile / install. 
