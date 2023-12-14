package kernel.jdon.moduleapi.domain.coffeechat.entity;

public enum CoffeeChatActiveStatus {
	OPEN("모집중"),
	FULL("모집완료"),
	CLOSE("모집종료");

	private final String activeStatus;

	CoffeeChatActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	public String getActiveStatus() {
		return activeStatus;
	}
}
