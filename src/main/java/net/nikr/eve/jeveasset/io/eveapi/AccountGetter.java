/*
 * Copyright 2009-2017 Contributors (see credits.txt)
 *
 * This file is part of jEveAssets.
 *
 * jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package net.nikr.eve.jeveasset.io.eveapi;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.model.account.Character;
import com.beimin.eveapi.response.account.ApiKeyInfoResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.nikr.eve.jeveasset.data.api.accounts.EveApiAccessMask;
import net.nikr.eve.jeveasset.data.api.accounts.EveApiAccount;
import net.nikr.eve.jeveasset.data.api.accounts.EveApiOwner;
import net.nikr.eve.jeveasset.gui.dialogs.update.UpdateTask;
import net.nikr.eve.jeveasset.io.shared.AccountAdder;


public class AccountGetter extends AbstractApiGetter<ApiKeyInfoResponse> implements AccountAdder  {

	private boolean limited = false;
	private boolean invalidPrivileges = false;
	private boolean wrongEntry = false;

	public AccountGetter() {
		super("Accounts", false, true);
	}

	public void load(final UpdateTask updateTask, final boolean forceUpdate, final EveApiAccount account) {
		limited = false;
		invalidPrivileges = false;
		wrongEntry = false;
		super.loadAccount(updateTask, forceUpdate, account);
	}

	public void load(final UpdateTask updateTask, final boolean forceUpdate, final List<EveApiAccount> accounts) {
		limited = false;
		invalidPrivileges = false;
		wrongEntry = false;
		super.loadAccounts(updateTask, forceUpdate, accounts);
	}

	@Override
	protected ApiKeyInfoResponse getResponse(final boolean bCorp) throws ApiException {
		return new com.beimin.eveapi.parser.account.ApiKeyInfoParser()
				.getResponse(EveApiOwner.getApiAuthorization(getAccount()));
	}

	@Override
	protected Date getNextUpdate() {
		return getAccount().getAccountNextUpdate();
	}

	@Override
	protected void setNextUpdate(final Date nextUpdate) {
		getAccount().setAccountNextUpdate(nextUpdate);
	}

	@Override
	protected void setData(final ApiKeyInfoResponse response) {
		//Changed between Char and Corp AKA should be treated as a new api
		boolean typeChanged = !getAccount().compareTypes(response.getType());

		//Update account
		getAccount().setAccessMask(response.getAccessMask());
		getAccount().setExpires(response.getExpires());
		getAccount().setType(response.getType());

		List<Character> characters = new ArrayList<Character>(response.getEveCharacters());
		List<EveApiOwner> owners = new ArrayList<EveApiOwner>();

		int fails = 0;
		int max = 0;
		if (isForceUpdate()) {
			max++;
			if (!getAccount().isAccountBalance()) {
				fails++;
			}
			max++;
			if (!getAccount().isIndustryJobs()) {
				fails++;
			}
			max++;
			if (!getAccount().isMarketOrders()) {
				fails++;
			}
			max++;
			if (!getAccount().isJournal()) {
				fails++;
			}
			max++;
			if (!getAccount().isTransactions()) {
				fails++;
			}
			max++;
			if (!getAccount().isContracts()) {
				fails++;
			}
			max++;
			if (!getAccount().isLocations()) {
				fails++;
			}
			max++;
			if (!getAccount().isAssetList()) {
				fails++;
			}
		}

		limited = (fails > 0 && fails < max);
		invalidPrivileges = (fails >= max);

		wrongEntry = !getAccount().getOwners().isEmpty();
		boolean canMigrate = false;
		for (Character apiCharacter : characters) {
			boolean found = false;
			for (EveApiOwner owner : getAccount().getOwners()) {
				if ((owner.getOwnerID() == apiCharacter.getCharacterID() || owner.getOwnerID() == apiCharacter.getCorporationID()) && !typeChanged) {
					owner.setOwnerName(getName(apiCharacter));
					owner.setOwnerID(getID(apiCharacter));
					owners.add(owner);
					found = true;
					wrongEntry = false;
					if (owner.canMigrate()) {
						canMigrate = true;
					}
					break;
				}
			}
			if (!found) { //Add New
				owners.add(new EveApiOwner(getAccount(), getName(apiCharacter), getID(apiCharacter)));
			}
		}
		if (canMigrate) {
			addError("EveApi", "Account can be migrated to ESI");
		}
		if (wrongEntry) {
			errorWrongEntry();
		} else {
			getAccount().setOwners(owners);
		}
	}

	@Override
	protected void updateFailed(final EveApiOwner ownerFrom, final EveApiOwner ownerTo) { }

	@Override
	protected long requestMask(boolean bCorp) {
		return EveApiAccessMask.OPEN.getAccessMask();
	}

	private String getName(final Character apiCharacter) {
		if (getAccount().isCharacter()) {
			return apiCharacter.getName();
		} else {
			return apiCharacter.getCorporationName();
		}
	}
	private long getID(final Character apiCharacter) {
		if (getAccount().isCharacter()) {
			return apiCharacter.getCharacterID();
		} else {
			return apiCharacter.getCorporationID();
		}
	}

	@Override
	public boolean isLimited() {
		return limited;
	}

	@Override
	public boolean isInvalidPrivileges() {
		return invalidPrivileges;
	}

	@Override
	public boolean isWrongEntry() {
		return wrongEntry;
	}
}
