/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.impl;

import org.ldp4j.application.spi.PersistencyManager;
import org.ldp4j.application.spi.Transaction;

import com.google.common.base.Objects;

final class InMemoryTransaction implements Transaction {

	private interface TransactionState {

		TransactionState begin();
		TransactionState commit();
		TransactionState rollback();
		boolean isStarted();
		boolean isCompleted();

	}

	private final class PendingTransactionState implements TransactionState {
		@Override
		public TransactionState begin() {
			return new InFlightTransactionState();
		}

		@Override
		public TransactionState commit() {
			throw new IllegalStateException("Transaction not initiated");
		}

		@Override
		public TransactionState rollback() {
			throw new IllegalStateException("Transaction not initiated");
		}

		@Override
		public String toString() {
			return "pending";
		}

		@Override
		public boolean isStarted() {
			return false;
		}

		@Override
		public boolean isCompleted() {
			return false;
		}

	}

	private final class InFlightTransactionState implements TransactionState {
		@Override
		public TransactionState begin() {
			throw new IllegalStateException("Transaction already initiated");
		}

		@Override
		public TransactionState commit() {
			persistencyManager.disposeTransaction(InMemoryTransaction.this);
			return new CompletedTransactionState("commited");
		}

		@Override
		public TransactionState rollback() {
			persistencyManager.disposeTransaction(InMemoryTransaction.this);
			return new CompletedTransactionState("rolledback");
		}

		@Override
		public String toString() {
			return "in-flight";
		}

		@Override
		public boolean isStarted() {
			return true;
		}

		@Override
		public boolean isCompleted() {
			return false;
		}
	}

	private final class CompletedTransactionState implements TransactionState {

		private String message;

		private CompletedTransactionState(String message) {
			this.message = message;
		}

		@Override
		public TransactionState begin() {
			throw new IllegalStateException("Transaction already finished");
		}

		@Override
		public TransactionState commit() {
			throw new IllegalStateException("Transaction already finished");
		}

		@Override
		public TransactionState rollback() {
			throw new IllegalStateException("Transaction already finished");
		}

		@Override
		public String toString() {
			return "completed ("+message+")";
		}

		@Override
		public boolean isStarted() {
			return true;
		}

		@Override
		public boolean isCompleted() {
			return true;
		}

	}

	private final InMemoryPersistencyManager persistencyManager;
	private final long id;
	private TransactionState state;

	InMemoryTransaction(long id, InMemoryPersistencyManager persistencyManager) {
		this.id = id;
		this.persistencyManager = persistencyManager;
		this.state=new PendingTransactionState();
	}

	long id() {
		return this.id;
	}

	@Override
	public PersistencyManager manager() {
		return this.persistencyManager;
	}

	@Override
	public void begin() {
		this.state=this.state.begin();
	}

	@Override
	public void commit() {
		this.state=this.state.commit();
	}

	@Override
	public void rollback() {
		this.state=this.state.rollback();
	}

	@Override
	public boolean isStarted() {
		return this.state.isStarted();
	}

	@Override
	public boolean isCompleted() {
		return this.state.isCompleted();
	}

	public String toString() {
		return
			Objects.
				toStringHelper(getClass()).
					add("id", this.id).
					add("state",this.state).
					add("persistencyManager",this.persistencyManager).
					toString();

	}

}
