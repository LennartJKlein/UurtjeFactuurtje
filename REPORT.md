# Final report
Met deze android app kan elke ZZP'er eenvoudig zelf zijn boekhouding doen. De ZZP'er houdt zijn projecten bij. Werk wat hij heeft gedaan voegt hij toe aan het project. Wanneer de tijd rijp is, klikt hij eenvoudigweg op 'afrekenen' en de app maakt een PDF met zijn factuur. Volledig volgens de standaarden van de belastingdienst.

![UurtjeFactuurtje -overview ](/docs/screenshot-overview.png?raw=true "UurtjeFactuurtje -overview ")  

## Technical design
### Technical overview
Na het inloggen via de `LoginActivity` wordt een gebruiker naar de `MainActivity` gebracht. Wanneer de gebruiker nieuw is, opent dan de `SettingsActivity` automatisch: daar kan de gebruiker zijn basisinstellingen direct invoeren.

##### `MainActivity`
Dit bestaat uit 3 tabbladen: Overzicht, BTW, Bedrijven.
1. **Overzicht** geeft een lijst met projecten van de gebruiker en zijn gemaakte kosten. (`OverviewFragment`)
2. **BTW** is een tool waarmee de gebruiker de informatie voor zijn BTW-aangifte kan opvragen. (`TaxFragment`)
3. **Bedrijven** is een pagina met de relaties van de ondernemer, inclusief zijn eigen bedrijf. (`CompaniesFragment`)

##### `ProjectActivity`
Dit bestaat uit 3 tabbladen: Werk, Facturen, Informatie.
1. **Werk** geeft een lijst met het werk wat voor dit project is uitgevoerd. De gebruiker kan hier werk toevoegen óf het nog onbetaalde werk afrekenen. De app genereert de PDF van een factuur die de gebruiker kan delen. (`ProjectWorkFragment`)
2. **Facturen** geeft een lijst met de gemaakte facturen voor dit project. De gebruiker kan de facturen vanuit hier als PDF openen. (`ProjectInvoiceFragment`)
3. **Informatie** geeft informatie over dit project, zoals startdatum en uurtarief. (`ProjectInfoFragment`)

##### `SettingsActivity`
Dit bestaat uit 3 tabbladen: Bedrijf, Nummers, Account.
1. Op de pagina **Bedrijf** kan de gebruiker gegevens van zijn bedrijf wijzigen zoals adres, bedrijfsnaam, etc. (`SettingsCompanyFragment`)
2. Op de pagina **Nummers** kan de gebruiker numerieke gegevens wijzigen zoals kvk, btw, betaaltermmijn, etc. (`SettingsNumbersFragment`)
3. Op de pagina **Account** kan de gebruiker zijn accountgegevens aanpassen. (`SettingsUserFragment`)

##### Data
Er zijn 4 soorten data die de gebruiker kan invoeren: Projecten, Werk, Aankopen, Relaties
1. **Project**. De gebruiker kiest een omschrijving, een opdrachtgever en een uurtarief. (`AddProjectFragment`)
2. **Werk**. De gebruiker kiest het bijbehorende project waarvoor is gewerkt. Daarna voert de gebruiker manuren (`AddWorkHoursFragment`) óf een product (`AddWorkProductFragment`) in. De totaalprijs wordt berekend en het werk wordt toegevoegd aan het project. (`AddWorkFragment`)
3. **Aankopen** De gebruiker kan hier onkosten van zijn onderneming invoeren. Benzine, software, bureaustoelen, etc. Deze worden vervolgens weergegeven in het Overzicht. (`AddCostFragment`)
4. **Relatie** De gebruiker kan relaties toevoegen. Dit kan zowel een opdrachtgever als een leverancier zijn. (`AddCompanyFragment`)

### Technical details
Om de pagina's van de app te ondersteunen, waren een aantal globale classes nodig.

##### Database objecten
* `User` is een class voor gebruikers
* `Company` is een class voor relaties *(gekoppeld aan een `User`)*
* `Cost` is een class voor aankopen / onkosten *(gekoppeld aan een `User` en een `Company`)*
* `Project` is een class voor projecten *(gekoppeld aan een `User` en een `Company`)*
* `Work` is een class voor werkzaamheden *(gekoppeld aan een `User`, een `Project` en een `Invoice`)*
* `Invoice` is een class voor facturen *(gekoppeld aan een `User`, een `Project` en een `Company`)*

##### Helper classes
* `GeneratePdf` genereert op basis van een `invoice` een PDF
* `PersistentDatabase` geeft een *singleton* instantie terug van Firebase Database. Dit is nodig om offline bewerkingen mogelijk te maken.

## Challenges and design descicions
* Verkleinen van de functionaliteiten tot behapbare grote.
	* Daardoor is de pagina 'BTW' niet gekoppeld aan het belastingsysteem van de overheid.
	* Daardoor worden straatnamen en woonplaatsen _niet_ automatisch ingeladen d.m.v. een API.
	* Daardoor heeft de app geen pincode beveiliging
	* Daardoor heeft de app geen iDeal-koppeling
	* Daardoor kan de gebruiker niet zijn bankrekeningstatus bijhouden
	* Daardoor heeft de app geen overzicht van 'te betalen' en 'te ontvangen' facturen
	* Daardoor zijn geëxporteerde PDF's niet aanpasbaar
* Er moest meer focus komen op *usability* in de app (ivm veel gebruikersinvoer).
	* Daardoor is er veel tijd gaan zitten in (opnieuw) vormgeven van schermen en invulvelden.
* ListViews bleken beperkt voor mijn wensen.
	* Ik moest me verdiepen in RecyclerViews
* De eerste databasestructuur was niet bruikbaar. Je kunt geen *queries* maken met Firebase.
	* De [web lesson van Jeff Daleney](https://angularfirebase.com/lessons/managing-firebase-user-relationships-to-database-records/) hielp om een betere structuur te vinden. Alles moest worden omgegooid.
* De bedrijfsgegevens toegevoegd aan het user-object.
* De 'instellingen-pagina' is nu niet een tabblad, maar een onderdeel van 'Mijn bedrijf' in de `CompaniesFragment`

Voor het invoeren van data worden altijd *dialogs* gebruikt. 