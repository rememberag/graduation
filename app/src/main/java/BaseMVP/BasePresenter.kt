package BaseMVP

class BasePresenter<T : BaseView, M : BaseModel> {

    constructor(view: T, model: M) {

    }

    fun create() {

    }
}