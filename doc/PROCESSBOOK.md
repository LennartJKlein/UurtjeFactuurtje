# Process book

### 23 december
##### Activities
* API vinden voor postcodes in Nederland. Uitzoeken hoe je een digitale koppeling kan maken met de Belastingdienst.
* Aanmelden bij Logius
* Aanmelden bij Aansluit Suite Digipoort
* Aanvraag insturen voor lidmaatschap CSO

### 27 december
##### Issues
Een prettige toevoeging voor de app, is de belasting automatisch kunnen aangeven via een digitale koppeling. Daarvoor is het nodig om lid te worden van de Community Software Ontwikkeling van de Nederlandse Overheid. Die aanvraag is op 27 dec helaas afgewezen. "Uit de door u aangeleverde gegevens kunnen wij niet opmaken dat u daadwerkelijk aangiftesoftware ontwikkelt c.q. gaat ontwikkelen." **Actie:** Een alternatieve feature voor de app bedenken, zodat de app op een andere manier helpt bij de digitale aangifte.

### 9 januari
##### Activities
* Project proposal schrijven voor de boekhouding app
* Schetsen maken van de verschillende schermen

##### Issues
De boekhouding app komt niet overeen met de wensen van de schoolopdracht: weinig invoer voor de gebruiker. **Actie:** er moet ter compensatie een sterke focus liggen op usability van het invoeren van de gegevens.

### 10 januari
##### Activities
* Mockups maken

##### Issues
Veel te veel functies. Flink korten op de functionaliteiten in de app. (zelfs de MVPs)

##### Decisions
* Er zal *geen* bankregister in de app komen. (dus betalingen van de facturen worden niet inbegrepen)
* Het inlogscherm zal *geen* pincode-variant bevatten (voor beveiliging tegen andere apparaatgebruikers)
* Er zal geen overzicht komen van crediteuren en debiteuren van facturen
* De factuur die wordt geëxporteerd is niet aanpasbaar

### 11 januari
##### Activities
* App-project opzetten
* Alle activities en fragments opzetten en waar mogelijk aan elkaar koppelen
* Eerste koppeling maken met Firebase om projecten uit te lezen
* Een floating action *menu* invoegen en klikbaar maken.

##### Issues
* In het verleden heb ik enkel ListViews gebruikt. Deze zijn te beperkt: de items in de lijst zijn moeilijk op te maken en naar wens aan te passen. Daarnaast verlangt het veel code om data uit de database in je view te krijgen.

##### Decisions
* Ik ga me verdiepen in RecyclerViews in combinatie met Firebase als inputbron.
* Ik laat het inloggen voor nu links liggen: het belangrijkste is dat men tijdens de presentatie kan zien hoe de app zelf in elkaar zit. Authenticatie kan als allerlaatste worden toegevoegd.

### 12 januari
##### Activities
* Presenteren van eerste navigatieschermen (incl. projectenlijst)

##### Issues
Eigenlijk geen feedback gehad op het project na afloop van mijn presentatie. Daarom ga ik gewoon maar voort op huidige voet.

### 15 januari
##### Activities
* Plan uitzetten voor alpha-versie van de app. Eerste focus moet liggen op het genereren van een PDF vanuit Java. "Do the hardest things first".

