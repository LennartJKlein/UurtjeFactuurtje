# Design document
Ontwerp van UurtjeFactuurtje, een android app voor boekhouding van ZZP'ers.

## Author
Een Android App project van [Lennart Klein](http://www.lennartklein.nl), in opdracht van de Universiteit van Amsterdam.
___________________________

## Problem statement
> Nederland telt ruim 1 miljoen zelfstandigen zonder personeel (zzp’ers) en dat worden er steeds meer. <sup>1</sup>

De werkzaamheden van deze groep lopen uiteen evenals de administratieve vaardigheden. Het bijhouden van een boekhouding is dan al snel ingewikkeld, moet worden ingelezen en belemmert het werken. De app's en diensten die momenteel op de markt zijn gaan uit van een bepaalde basiskennis die niet bij elke ZZP'er aanwezig is. <sup>2</sup> <sup>3</sup> <sup>4</sup>
* <sup>1</sup> CBS, "Dossier ZZP". https://www.cbs.nl/nl-nl/dossier/dossier-zzp
* <sup>2</sup> Thinq, "Boekhouding App". https://www.thinq.nl/hoe-werkt-het/boekhouding-app
* <sup>3</sup> Offective, "Online Boekhouden". https://www.offective.nl/online-administratie-software/online-boekhouden
* <sup>4</sup> Informer, "Boekhoudprogramma". https://www.informer.nl/

## Similar apps
### E-boekhouden
https://play.google.com/store/apps/details?id=eboekhouden.nl&hl=nl

| Good | Bad |
| ---- | --- |
| Veilige inlogmethodes: pincode, fingerprint | Interface voelt niet intuïtief |
| Overzicht van winst en verlies per maand | Weinig inzicht in de features voor nieuwe gebruikers |
| Openstaande betalingen direct inzichtelijk | Veel in te stellen als ondernemer |

### InformerOnline
https://play.google.com/store/apps/details?id=nl.Informer.app

| Good | Bad |
| ---- | --- |
| Duidelijk startscherm met grote buttons | Design niet duidelijk. Logo van de app blijft onnodig in beeld. |
| Informatie invoeren duidelijk met imagebuttons | App is slechts te gebruiken voor invoeren van gegevens. Niet voor gegevens eruit lezen (bijv. BTW of debiteuren / crediteuren) | 

## Solution
De oplossing is een enorm simpele app met een moderne vormgeving die boekhouding beperkt tot een simpel kassa-systeem. De ZZP'er hoeft slechts in te voeren en het systeem doet het denkwerk. Het houdt de ondernemer op de hoogte wanneer belasting moet worden aangegeven en welke velden daarvoor moeten worden ingevuld. De ZZP'er hoeft nu alleen nog verstand te hebben van zijn eigen werk.

### Requirements
De onderstaande requirements voor de app zijn volgens de MoSCoW-methode opgedeeld.
De requirements zijn zo opgesteld, dat in de 4 weken van dit project in ieder geval de 'must have' en eventueel 'should have' gemaakt kunnen worden. De rest van de requirements zijn todo's voor de toekomst.

#### Must have (MVP)
* App moet te gebruiken zijn op smartphone & tablet
* Inloggen / registreren (via Firebase)
* Bedrijfsgegevens invoeren (gekoppeld aan Firebase-account)
* Uurprijzen invoeren (gekoppeld aan Firebase-account)
* Projecten aanmaken
* Projectdetails invoeren
* Gemaakte uren + uurtarief + beschrijving van activiteiten toevoegen aan project
* Gemaakte kosten toevoegen aan project
* Factuur voor een project genereren
* Bedrijfskosten invoeren (m.b.v. boekstuknummer ook fysiek te bewaren)
* Een output van boekhouding met juiste info voor BTW-aangifte

#### Should have
* Invoeren van betalingen (inkomsten en uitgaven op bankrekening)
* Overzicht van inkomsten, uitgaven, kosten, winst
* Contactgegevens bij project toevoegen
* Project afsluiten

#### Could have
* Verkoopbare producten invoeren en aantallen bijhouden
* Verkochte producten toevoegen aan project
* Notificatie wanneer BTW-aangifte moet geschieden
* Bijhouden van afspraken via Google Calendar implementatie
* 'Hulp-scherm' met uitleg over begrippen in de app & hoe boekhouding voor ZZP'ers werkt
* Berekening voor 'Kleine ondernemersregeling'

#### Nice to have
* Live timer tijdens het werken aan project
* Foto van bonnen / facturen toevoegen
* PDF van bonnen / facturen toevoegen
* Backup van gehele boekhouding maken en naar e-mail versturen
* Verschillende uurprijzen per project
* Bepaalde features (timer, uren invoeren) van de app ook beschikbaar voor Android Wear
* Periodieke kosten (wekelijks, maandelijks, jaarlijks) worden automatisch verwerkt
* Vaste Activa & afschrijving daarvan bijhouden

### Hardest parts
Het koppelen van gemaakte uren aan een project, de datum daarvan onthouden en op basis daarvan een factuur genereren. Het maken van die factuur is op zichzelf ook nog een uitdaging. Er moet namelijk een PDF op A4-formaat worden gegenereerd, welke wordt opgeslagen en per mail moet worden verstuurd.

De grootste uitdaging opgesomd: PDF genereren, datums vergelijken, data koppelen aan andere data en bovenal: begrijpelijke en eenvoudige interface.

## Database structure

#### Users
* ID
  * name
  * e-mail
  * phone
  * payment_days

#### Companies
* ID
  * `user ID`
  * name
  * postal_code
  * street
  * building_nr
  * city
  * bank
  * btw_nr
  * kvk_nr
  * website
  * email
  * person

#### Projects
* ID
  * `user ID`
  * `company ID`
  * name
  * start_date
  * last_invoice
  * hour_price

#### Invoices
* ID
  * `user ID`
  * `project ID`
  * `company ID`
  * sender
  * invoice_nr
  * date
  * subtotal
  * btw
  * total

#### Work
* ID
  * `user ID`
  * `project ID`
  * `invoice ID`
  * date
  * description
  * hours
  * price
  * paid

#### Memorial
* ID
  * `user ID`
  * bank_balance
  * invoice_nr

## Classes

### Login / register
| AccountActivity |
| ---- |
| setFragment() |

| LoginFragment |
| ---- |
| logIn() checkAuth() |

| RegisterFragment |
| ---- |
| checkAuth() register() |

### Main
| MainActivity |
| ---- |
| setFragment() |

### Overview
| OverviewFragment |
| ---- |
| initiateProjects() getCosts() showProject() initiateActionButton() addWork() addCost() addProject() |

| AddProjectFragment |
| ---- |
| getCompanies() addProject() |

| AddCostFragment |
| ---- |
| addCost() |

| AddWorkFragment |
| ---- |
| getProjects() addWork() |

### BTW
| BTWFragment |
| ---- |
| checkDates() getInvoices() calculateGive() calculateRevenue() calculateGet() |

### Relaties
| RelationsFragment |
| ---- |
| getRelations() addRelation() |

| AddRelationFragment |
| ---- |
| addRelation() |

### Project
| ProjectActivity |
| ---- |
| setFragment() navigateBack() |

| ProjectWorkFragment |
| ---- |
| getUnpaidWork() addInvoice() showInvoice() |

| ProjectInvoicesFragment |
| ---- |
| getInvoices() showInvoice() |

| ProjectInfoFragment |
| ---- |
| getProjectInfo() |

| InvoiceFragment |
| ---- |
| getInvoice() |

### Settings
| SettingsActivity |
| ---- |
| setFragment() |

| TutorialFragment |
| ---- |
| launchNextTab() |

| SettingsCompanyFragment |
| ---- |
| getAddress() setCompanySettings() launchNextTab() |

| SettingsFinanceFragment |
| ---- |
| setFinanceSettings() launchNextTab() |

| SettingsProgramFragment |
| ---- |
| setProgramSettings() launchNextTab() |

| SettingsUserFragment |
| ---- |
| setUserSettings() launchNextTab() |

## External components / libraries
* Firebase (https://firebase.google.com/)
* Firebase UI
* iText PDF generator (https://github.com/itext)
* Clans - FloatingActionButton (https://github.com/Clans/FloatingActionButton)
* Lesilva - BetterSpinner (https://github.com/Lesilva/BetterSpinner)

## API's
* Postcode API (https://www.postcodeapi.nu/)

## Sketches
[![](docs/sketch-0-small.jpg)](/docs/sketch-0.jpg?raw=true)
[![](docs/sketch-1-small.jpg)](/docs/sketch-1.jpg?raw=true)

## Screens and flow chart
[![](doc/flowchart-1.0-small.jpg)](/docs/flowchart-1.0.jpg?raw=true)

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details