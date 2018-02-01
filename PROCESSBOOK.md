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

### 18 januari
##### Issues
* Nu de eerste schermen werken, blijkt het met de huidige opzet van de database moeilijk om alleen de bedrijven van déze gebruiker te laten zien. De [web lesson van Jeff Daleney](https://angularfirebase.com/lessons/managing-firebase-user-relationships-to-database-records/) gaf een goed voorbeeld. **Actie:** sla bedrijven, projecten, facturen op in een 'submap' met het UserID als titel.
* Bedrijf van de gebruiker was eerst een 'company' met een labeltje dat het van de gebruiker was. Dit bleek totaal onpraktisch voor het zoeken van de gegevens van 'mijn bedrijf'. Daarom zijn de bedrijfsgegevens toegevoegd aan het user-object.

### 19 januari
#### Design descisions
* Het instellingen tabblad in de bottomnavigation voelt wat onnatuurlijk. De flow ervan is anders dan de andere tabbladen: er opent namelijk een heel nieuw scherm. Daarnaast verwacht de gebruiker de informatie over zijn bedrijf niet onder 'instellingen'. Het alternatief is: de pagina 'relaties' ombouwen naar 'bedrijven' en daar ook een linkje voor 'mijn bedrijf' toevoegen. Die verwijst vervolgens naar de instellingenpagina.

### 22 januari
#### Design descisions
* Tip van Jan Erik: voeg een 'weet je het zeker?' toe aan verwijderen van relatie, project, etc.
* De pagina 'bedrijven' heeft een floating action button om een bedrijf toe te voegen. Deze bleek onduidelijk voor gebruikers: hij wordt verwardt met de FAB op de overzichtspagina. Daarom krijgt de FAB op de bedrijven-pagina een andere layout (mede omdat hij alleen van toepassing is op de lijst 'relaties' en niet op 'mijn bedrijf')
* Tutorial over de app komt niet als tabblad van 'mijn bedrijf'. De tutorial met werking van de app moet op een andere plek komen.
* Het tabblad 'programma instellingen' wordt 'nummers'
* Het instellingenveld 'website' verhuist naar het tabblad 'nummers'

### 23 januari
#### Design descisions
* De labels van inputvelden zijn inconsistent. De labels en velden lijken te zweven. Een betere floating label, die visueel gelinkt blijft aan een veld, zou helpen. Wellicht is het vervangen van de floating label door een icoon beter en duidelijker. (zeker omdat het voor de gebruiker even duurt om te bedenken wat er in een veld ingevuld moet worden)

### 24 januari
#### Design descisions
* Het formulier voor het toevoegen van manuren kan intuitiever. Eerst was er een veld voor het aantal werkuren (als kommagetal). Dit is niet handig en vergt extra denkwerk voor de gebruiker. Beter is om 2 velden te maken: een begintijd en een eindtijd.
* De app moet ook werken wanneer internet even niet beschikbaar is. Daarom een PersistentDatabase class door de hele app getrokken, zodat er een offline variant van de database altijd beschikbaar is.

### 25 januari
#### Issues
* Ik ben vrij lang bezig met het werkend krijgen van 'werk' toevoegen. Dit deel bleek gecompliceerder dan verwacht. Een aantal features lopen nu achter, zoals 'kosten' en 'btw'. Deze zullen waarschijnlijk niet klaar zijn voor de beta-versie.

### 29 januari
#### Issues
* Het systeem van facturen maken tot PDF bleek buggy'er dan verwacht. Er zaten een hoop fouten in, dat als de gebruiker iets onverwachts deed, het systeem daar niet mee overweg kon. Dit heeft me uiteindelijk de hele dag gekost, terwijl ik eigenlijk nog MVP-features moest toevoegen.

#### Design descisions
* Het genereren van PDF's was onhandig opgezet: de file-generator zelf ging nog calls maken naar FireBase om delen van de informatie van het bestand op te halen. Actie: alle data verzamelen vóór het naar de generator gaat.

### 30 januari
Vliegende start. Facturen jzijn gerepareerd, 'kosten' zijn toegevoegd en hebben hun eigen invoerscherm.

#### Design descisions
* Het woord 'kost' en 'kosten' was verwarrend. Dit merkte ik bij het uitleggen van mijn voortgang aan mijn team. De nieuwe Nederlandse term wordt nu 'aankoop', 'aankopen', 'aanschaf'.

### 1 februari

#### Design descisions
* De android projectbestanden heb ik toch maar in de hoofdmap gezet, in plaats van een aparte 'app' directory. Dit gaf bij het programma zelf al verwarring en daarnaast is het niet gebruikelijk.